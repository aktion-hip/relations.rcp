/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2026, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.peer.libp2p;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.libp2p.core.Stream;
import io.libp2p.core.multistream.StrictProtocolBinding;
import io.libp2p.protocol.ProtocolHandler;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** The sending half of <code>/relations/export/1.0.0</code>.
 *
 * <p>
 * Relations listens; the companion application dials and opens the stream. After the user
 * approves the connecting device this writes the header and the payload, then waits for the
 * receiver's acknowledgement. The wire format is specified in <code>PROTOCOL.md</code>,
 * which is the contract the receiving implementation is written against.
 * </p>
 *
 * @author lbenno */
public class ExportSender extends StrictProtocolBinding<ExportSender.Session> {

    /** The protocol id. Any change to the framing is a new id, never a redefinition of
     * this one. */
    public static final String PROTOCOL_ID = "/relations/export/1.0.0"; //$NON-NLS-1$

    private static final byte[] MAGIC_HEADER = { 'R', 'L', 'E', 'X' };
    private static final byte[] MAGIC_ACK = { 'R', 'L', 'A', 'K' };
    private static final byte DIGEST_SHA_256 = 0x01;
    private static final int ACK_FIXED_LENGTH = 7;
    private static final int CHUNK_SIZE = 64 * 1024;

    /** ExportSender constructor.
     *
     * @param offerSupplier {@link Supplier}&lt;{@link ExportOffer}> supplies the export to
     *            send, evaluated once a device has been approved
     * @param approvalGate {@link ApprovalGate} asks the user about the connecting device
     * @param sessionListener {@link Consumer}&lt;{@link Session}> notified as each transfer
     *            starts, so the caller can observe its outcome and decide whether the change
     *            log may be cleared */
    public ExportSender(final Supplier<ExportOffer> offerSupplier, final ApprovalGate approvalGate,
            final Consumer<Session> sessionListener) {
        super(PROTOCOL_ID, new Handler(offerSupplier, approvalGate, sessionListener));
    }

    // ---

    private static class Handler extends ProtocolHandler<Session> {
        private final Supplier<ExportOffer> offerSupplier;
        private final ApprovalGate approvalGate;
        private final Consumer<Session> sessionListener;

        Handler(final Supplier<ExportOffer> offerSupplier, final ApprovalGate approvalGate,
                final Consumer<Session> sessionListener) {
            super(Long.MAX_VALUE, Long.MAX_VALUE);
            this.offerSupplier = offerSupplier;
            this.approvalGate = approvalGate;
            this.sessionListener = sessionListener;
        }

        @Override
        protected CompletableFuture<Session> onStartResponder(final Stream stream) {
            final Session session = new Session(stream, this.offerSupplier, this.approvalGate);
            this.sessionListener.accept(session);
            stream.pushHandler(session);
            return CompletableFuture.completedFuture(session);
        }

        @Override
        protected CompletableFuture<Session> onStartInitiator(final Stream stream) {
            // Relations never dials; the companion application opens the stream.
            stream.reset();
            return CompletableFuture.failedFuture(
                    new UnsupportedOperationException("Relations does not initiate peer transfers")); //$NON-NLS-1$
        }
    }

    /** One transfer over one stream. */
    public static class Session implements ProtocolMessageHandler<ByteBuf> {

        private final Stream stream;
        private final Supplier<ExportOffer> offerSupplier;
        private final ApprovalGate approvalGate;
        private final CompletableFuture<TransferOutcome> outcome = new CompletableFuture<>();
        private final ByteArrayOutputStream ackBytes = new ByteArrayOutputStream();

        Session(final Stream stream, final Supplier<ExportOffer> offerSupplier,
                final ApprovalGate approvalGate) {
            this.stream = stream;
            this.offerSupplier = offerSupplier;
            this.approvalGate = approvalGate;
        }

        /** @return {@link CompletableFuture}&lt;{@link TransferOutcome}> completes when the
         *         transfer ends, however it ends */
        public CompletableFuture<TransferOutcome> getOutcome() {
            return this.outcome;
        }

        /** @return String the authenticated identity of the connected device */
        public String getRemotePeerId() {
            return this.stream.remotePeerId().toBase58();
        }

        @Override
        public void onActivated(final Stream activated) {
            this.approvalGate.approve(getRemotePeerId())
                    .thenAccept(approved -> {
                        if (Boolean.TRUE.equals(approved)) {
                            sendExport();
                        } else {
                            finish(TransferOutcome.DECLINED_BY_USER);
                            activated.close();
                        }
                    })
                    .exceptionally(exc -> {
                        finish(TransferOutcome.PROTOCOL_ERROR);
                        activated.reset();
                        return null;
                    });
        }

        @Override
        public void onMessage(final Stream from, final ByteBuf message) {
            final byte[] incoming = new byte[message.readableBytes()];
            message.readBytes(incoming);
            this.ackBytes.write(incoming, 0, incoming.length);
            tryCompleteAck(from);
        }

        @Override
        public void onClosed(final Stream closed) {
            // A close before the acknowledgement means the transfer did not complete.
            finish(TransferOutcome.INTERRUPTED);
        }

        @Override
        public void onException(final Throwable cause) {
            finish(TransferOutcome.INTERRUPTED);
        }

        private void sendExport() {
            try {
                final ExportOffer offer = this.offerSupplier.get();
                final long length = Files.size(offer.getFile());
                this.stream.writeAndFlush(Unpooled.wrappedBuffer(
                        buildHeader(offer, length, digestOf(offer))));
                writePayload(offer);
            }
            catch (final IOException | NoSuchAlgorithmException exc) {
                finish(TransferOutcome.PROTOCOL_ERROR);
                this.stream.reset();
            }
        }

        private void writePayload(final ExportOffer offer) throws IOException {
            try (InputStream source = Files.newInputStream(offer.getFile())) {
                final byte[] chunk = new byte[CHUNK_SIZE];
                int read;
                while ((read = source.read(chunk)) > 0) {
                    this.stream.writeAndFlush(Unpooled.copiedBuffer(chunk, 0, read));
                }
            }
        }

        private byte[] buildHeader(final ExportOffer offer, final long length, final byte[] digest) {
            final byte[] name = offer.getName().getBytes(StandardCharsets.UTF_8);
            final ByteBuffer header = ByteBuffer
                    .allocate(MAGIC_HEADER.length + 1 + 8 + 1 + 1 + digest.length + 2 + name.length);
            header.put(MAGIC_HEADER);
            header.put(offer.getScope().getWireValue());
            header.putLong(length);
            header.put(DIGEST_SHA_256);
            header.put((byte) digest.length);
            header.put(digest);
            header.putShort((short) name.length);
            header.put(name);
            return header.array();
        }

        private byte[] digestOf(final ExportOffer offer) throws IOException, NoSuchAlgorithmException {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
            try (InputStream source = Files.newInputStream(offer.getFile())) {
                final byte[] chunk = new byte[CHUNK_SIZE];
                int read;
                while ((read = source.read(chunk)) > 0) {
                    digest.update(chunk, 0, read);
                }
            }
            return digest.digest();
        }

        private void tryCompleteAck(final Stream from) {
            final byte[] received = this.ackBytes.toByteArray();
            if (received.length < ACK_FIXED_LENGTH) {
                return;
            }
            if (!Arrays.equals(Arrays.copyOf(received, MAGIC_ACK.length), MAGIC_ACK)) {
                finish(TransferOutcome.PROTOCOL_ERROR);
                from.reset();
                return;
            }
            final ByteBuffer ack = ByteBuffer.wrap(received);
            ack.position(MAGIC_ACK.length);
            final byte status = ack.get();
            final int messageLength = Short.toUnsignedInt(ack.getShort());
            if (ack.remaining() < messageLength) {
                return; // diagnostic text still arriving
            }
            finish(outcomeOf(status));
            from.close();
        }

        private static TransferOutcome outcomeOf(final byte status) {
            return switch (status) {
                case 0x00 -> TransferOutcome.COMPLETED;
                case 0x01 -> TransferOutcome.DIGEST_MISMATCH;
                case 0x02 -> TransferOutcome.STORAGE_ERROR;
                case 0x03 -> TransferOutcome.REJECTED_BY_RECEIVER;
                default -> TransferOutcome.PROTOCOL_ERROR;
            };
        }

        private void finish(final TransferOutcome result) {
            this.outcome.complete(result);
        }
    }
}
