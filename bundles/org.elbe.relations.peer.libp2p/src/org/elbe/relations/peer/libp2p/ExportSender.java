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
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.parsson.JsonProviderImpl;

import io.libp2p.core.Stream;
import io.libp2p.core.multistream.StrictProtocolBinding;
import io.libp2p.protocol.ProtocolHandler;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;

/** The sending half of <code>/relations/sync/1.0.0</code>.
 *
 * <p>
 * Relations listens; the companion application dials and opens the stream. Relations sends
 * <code>hello</code>, the phone answers with a <code>request</code>, and once the user has
 * approved the connecting device this sends the <code>manifest</code> followed by the raw
 * file contents, then waits for the phone's <code>result</code>. The wire format is
 * specified in <code>PROTOCOL.md</code>, which is the contract the receiving implementation
 * is written against.
 * </p>
 *
 * @author lbenno */
public class ExportSender extends StrictProtocolBinding<ExportSender.Session> {

    /** The protocol id. Any change to the framing is a new id, never a redefinition of
     * this one. */
    public static final String PROTOCOL_ID = "/relations/sync/1.0.0"; //$NON-NLS-1$

    /** The version carried in the <code>protocol</code> field of <code>hello</code> and
     * <code>request</code>. */
    static final int PROTOCOL_VERSION = 1;

    /** Created directly rather than through <code>JsonProvider.provider()</code>: Parsson
     * registers itself only through the OSGi ServiceLoader Mediator, so the lookup fails
     * inside the product. Both jars are on this bundle's class path. */
    private static final JsonProvider JSON = new JsonProviderImpl();

    private static final String MODE_FULL = "full"; //$NON-NLS-1$
    private static final String MODE_INCREMENTAL = "incremental"; //$NON-NLS-1$

    /** ExportSender constructor.
     *
     * @param files {@link ExportFiles}
     * @param approvalGate {@link ApprovalGate} asks the user about the connecting device
     * @param observer {@link TransferObserver} notified as each transfer starts, so the caller can observe its
     *            outcome and decide whether the change log may be cleared */
    public ExportSender(final ExportFiles files, final ApprovalGate approvalGate, final TransferObserver observer) {
        super(PROTOCOL_ID, new Handler(files, approvalGate, observer, computerName()));
    }

    private static String computerName() {
        final String name = System.getenv("COMPUTERNAME"); //$NON-NLS-1$ Windows
        if (name != null && !name.isBlank()) {
            return name;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final IOException exc) {
            return "Relations"; //$NON-NLS-1$
        }
    }

    /** The 6-digit code both devices display, see "Confirmation code" in PROTOCOL.md.
     *
     * @param peerA byte[] the raw bytes of one peer id
     * @param peerB byte[] the raw bytes of the other peer id
     * @return String six decimal digits, independent of the order of the arguments */
    static String confirmationCode(final byte[] peerA, final byte[] peerB) {
        final boolean aFirst = Arrays.compareUnsigned(peerA, peerB) <= 0;
        try {
            final MessageDigest sha = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
            sha.update(aFirst ? peerA : peerB);
            sha.update(aFirst ? peerB : peerA);
            final long value = Integer.toUnsignedLong(ByteBuffer.wrap(sha.digest(), 0, 4).getInt());
            return String.format(Locale.ROOT, "%06d", value % 1_000_000); //$NON-NLS-1$
        } catch (final NoSuchAlgorithmException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /** Writes a frame: 4-byte big-endian length of the UTF-8 JSON, then the JSON. */
    private static ByteBuf frame(final JsonObject json) {
        final byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);
        final ByteBuffer frame = ByteBuffer.allocate(4 + body.length); // big-endian
        frame.putInt(body.length).put(body);
        return Unpooled.wrappedBuffer(frame.array());
    }

    private static JsonObjectBuilder error(final String code, final String message) {
        return JSON.createObjectBuilder()
                .add("type", "error") //$NON-NLS-1$ //$NON-NLS-2$
                .add("code", code) //$NON-NLS-1$
                .add("message", message); //$NON-NLS-1$
    }

    // ---

    private static class Handler extends ProtocolHandler<Session> {
        private final ExportFiles exportFiles;
        private final ApprovalGate approvalGate;
        private final TransferObserver observer;
        private final String computerName;
        /** The running transfer, only one at a time. */
        private final AtomicReference<Session> active = new AtomicReference<>();

        /** @param exportFiles ExportFiles provides the files for the phone's request
         * @param computerName String the name the phone displays in its confirmation dialog */
        Handler(final ExportFiles exportFiles, final ApprovalGate approvalGate, final TransferObserver observer,
                final String computerName) {
            super(Long.MAX_VALUE, Long.MAX_VALUE);
            this.exportFiles = exportFiles;
            this.approvalGate = approvalGate;
            this.observer = observer;
            this.computerName = computerName;
        }

        @Override
        protected CompletableFuture<Session> onStartResponder(final Stream stream) {
            final Session running = this.active.get();
            if (running != null && !running.getOutcome().isDone()) {
                // another phone (or a second stream) while a transfer runs
                stream.writeAndFlush(frame(error("busy", "Another transfer is running.").build())); //$NON-NLS-1$ //$NON-NLS-2$
                stream.close();
                return CompletableFuture.failedFuture(
                        new IllegalStateException("A transfer is already running")); //$NON-NLS-1$
            }
            try {
                final Session session = new Session(stream, this.computerName, this.exportFiles,
                        this.approvalGate);
                this.active.set(session);
                this.observer.started(session);
                // first, so that the session can wait for its writes (back-pressure)
                stream.pushHandler(new ChannelCapture(session));
                stream.pushHandler(session);
                return CompletableFuture.completedFuture(session);
            } catch (final Throwable exc) {
                // must not vanish on the networking thread: report it, then drop the stream
                this.observer.failedToStart(stream.remotePeerId().toBase58(), exc);
                stream.reset();
                return CompletableFuture.failedFuture(exc);
            }
        }

        @Override
        protected CompletableFuture<Session> onStartInitiator(final Stream stream) {
            // Relations never dials; the phone opens the stream.
            stream.reset();
            return CompletableFuture.failedFuture(
                    new UnsupportedOperationException("Relations does not initiate peer transfers")); //$NON-NLS-1$
        }
    }

    /** Hands the stream's Netty channel to the session, whose own writes return no future. */
    private static class ChannelCapture extends ChannelInboundHandlerAdapter {
        private final Session session;

        ChannelCapture(final Session session) {
            this.session = session;
        }

        @Override
        public void handlerAdded(final ChannelHandlerContext ctx) {
            this.session.channel = ctx.channel();
        }
    }

    /** One transfer over one stream. */
    public static class Session implements ProtocolMessageHandler<ByteBuf> {

        private static final int MAX_FRAME = 65_536;
        private static final int CHUNK_SIZE = 64 * 1024;

        private final Stream stream;
        private final String computerName;
        private final ExportFiles exportFiles;
        private final ApprovalGate approvalGate;
        private final CompletableFuture<TransferOutcome> outcome = new CompletableFuture<>();
        /** Completes with true for an incremental, false for a full request. */
        private final CompletableFuture<Boolean> request = new CompletableFuture<>();
        private final ByteArrayOutputStream pending = new ByteArrayOutputStream();
        private volatile CompletableFuture<Boolean> approval;
        private volatile Channel channel;
        /** The names announced in the manifest, null before it was sent. */
        private volatile List<String> sentNames;
        private volatile Throwable failure;
        private boolean requested;

        /** @param computerName String the name the phone displays, e.g.
         *            {@code InetAddress.getLocalHost().getHostName()} */
        Session(final Stream stream, final String computerName, final ExportFiles exportFiles,
                final ApprovalGate approvalGate) {
            this.stream = stream;
            this.computerName = computerName;
            this.exportFiles = exportFiles;
            this.approvalGate = approvalGate;
        }

        /** @return {@link CompletableFuture}&lt;{@link TransferOutcome}> completes when the transfer ends, however it
         *         ends */
        public CompletableFuture<TransferOutcome> getOutcome() {
            return this.outcome;
        }

        /** @return Throwable the error that ended the transfer, for the log, or <code>null</code> */
        public Throwable getFailure() {
            return this.failure;
        }

        /** @return String the authenticated identity of the connected device */
        public String getRemotePeerId() {
            return this.stream.remotePeerId().toBase58();
        }

        @Override
        public void onActivated(final Stream activated) {
            // 1. the desktop speaks first, without waiting for anything
            send(JSON.createObjectBuilder()
                    .add("type", "hello") //$NON-NLS-1$ //$NON-NLS-2$
                    .add("protocol", PROTOCOL_VERSION) //$NON-NLS-1$
                    .add("name", this.computerName) //$NON-NLS-1$
                    .build());
            // 2. show the same code as the phone and ask the user (in parallel to the user on the phone);
            // both ids as authenticated by Noise on this connection
            final String code = confirmationCode(
                    activated.getConnection().secureSession().getLocalId().getBytes(),
                    activated.remotePeerId().getBytes());
            final CompletableFuture<Boolean> asked = this.approvalGate.approve(getRemotePeerId(), code);
            this.approval = asked;
            if (this.outcome.isDone()) {
                // ended while the gate was being set up
                asked.cancel(false);
                return;
            }
            asked.whenComplete((approved, exc) -> {
                if (this.outcome.isDone() || exc instanceof CancellationException) {
                    return;
                }
                if (exc != null) {
                    fail(TransferOutcome.PROTOCOL_ERROR, exc);
                } else if (!Boolean.TRUE.equals(approved)) {
                    sendError("declined", "The transfer was declined on the computer."); //$NON-NLS-1$ //$NON-NLS-2$
                    finish(TransferOutcome.DECLINED_BY_USER);
                }
            });
            // 3. send the data once both users have accepted (the phone's request means its user accepted);
            // async: reading the files must not block the Netty event loop
            asked.thenAcceptBothAsync(this.request, (approved, incremental) -> {
                if (Boolean.TRUE.equals(approved) && !this.outcome.isDone()) {
                    sendExport(incremental);
                }
            }).exceptionally(exc -> {
                final Throwable cause = exc instanceof CompletionException && exc.getCause() != null
                        ? exc.getCause()
                        : exc;
                if (!(cause instanceof CancellationException)) {
                    fail(TransferOutcome.PROTOCOL_ERROR, cause);
                }
                return null;
            });
        }

        @Override
        public void onMessage(final Stream from, final ByteBuf message) {
            if (this.outcome.isDone()) {
                return;
            }
            final byte[] incoming = new byte[message.readableBytes()];
            message.readBytes(incoming);
            this.pending.write(incoming, 0, incoming.length);
            readFrames();
        }

        @Override
        public void onClosed(final Stream closed) {
            // A close before the result means the transfer did not complete.
            finish(TransferOutcome.INTERRUPTED);
        }

        @Override
        public void onException(final Throwable cause) {
            fail(TransferOutcome.INTERRUPTED, cause);
        }

        // --- phone -> desktop: request, then result or error

        private void readFrames() {
            final byte[] data = this.pending.toByteArray();
            int pos = 0;
            while (data.length - pos >= 4) {
                final long length = Integer.toUnsignedLong(ByteBuffer.wrap(data, pos, 4).getInt());
                if (length > MAX_FRAME) {
                    protocolError("Frame of " + length + " bytes is too large."); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                if (data.length - pos - 4 < length) {
                    break; // frame still arriving
                }
                final String json = new String(data, pos + 4, (int) length, StandardCharsets.UTF_8);
                pos += 4 + (int) length;
                final JsonObject frame;
                try (JsonReader reader = JSON.createReader(new StringReader(json))) {
                    frame = reader.readObject();
                } catch (final JsonException exc) {
                    // not JSON, or not a JSON object
                    protocolError("Invalid JSON."); //$NON-NLS-1$
                    return;
                }
                if (!onFrame(frame)) {
                    return;
                }
            }
            this.pending.reset();
            this.pending.write(data, pos, data.length - pos);
        }

        /** @return boolean false if the session ended because of the frame */
        private boolean onFrame(final JsonObject json) {
            switch (json.getString("type", "")) { //$NON-NLS-1$ //$NON-NLS-2$
                case "request" -> { //$NON-NLS-1$
                    return onRequest(json);
                }
                case "result" -> { //$NON-NLS-1$
                    final List<String> sent = this.sentNames;
                    final List<String> names = importedNames(json.get("imported")); //$NON-NLS-1$
                    if (sent == null || names == null) {
                        protocolError("Unexpected result."); //$NON-NLS-1$
                        return false;
                    }
                    this.exportFiles.imported(names);
                    finish(names.containsAll(sent) ? TransferOutcome.COMPLETED : TransferOutcome.REJECTED_BY_RECEIVER);
                    return false;
                }
                case "error" -> { //$NON-NLS-1$
                    this.failure = new IOException("Receiver reported: " //$NON-NLS-1$
                            + json.getString("code", "") + " " + json.getString("message", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    finish(switch (json.getString("code", "")) { //$NON-NLS-1$ //$NON-NLS-2$
                        case "digest-mismatch" -> TransferOutcome.DIGEST_MISMATCH; //$NON-NLS-1$
                        case "storage" -> TransferOutcome.STORAGE_ERROR; //$NON-NLS-1$
                        default -> TransferOutcome.REJECTED_BY_RECEIVER;
                    });
                    return false;
                }
                default -> {
                    protocolError("Unexpected message."); //$NON-NLS-1$
                    return false;
                }
            }
        }

        private boolean onRequest(final JsonObject json) {
            if (this.requested) {
                protocolError("Unexpected request."); //$NON-NLS-1$
                return false;
            }
            this.requested = true;
            // the version before anything else, so a future phone gets a meaningful answer
            final int version = json.get("protocol") instanceof final JsonNumber number //$NON-NLS-1$
                    ? number.intValue()
                    : 0;
            if (version != PROTOCOL_VERSION) {
                send(error("version-mismatch", //$NON-NLS-1$
                        "This computer supports protocol version " + PROTOCOL_VERSION + " only.") //$NON-NLS-1$ //$NON-NLS-2$
                        .add("supported", PROTOCOL_VERSION) //$NON-NLS-1$
                        .build());
                this.failure = new IOException("Receiver requested protocol version " + version); //$NON-NLS-1$
                finish(TransferOutcome.VERSION_MISMATCH);
                return false;
            }
            final String mode = json.getString("mode", ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (!MODE_FULL.equals(mode) && !MODE_INCREMENTAL.equals(mode)) {
                protocolError("Unexpected request."); //$NON-NLS-1$
                return false;
            }
            // the scope the user prepared is authoritative
            final boolean incremental = MODE_INCREMENTAL.equals(mode);
            if (incremental != this.exportFiles.offersIncremental()) {
                final String offered = this.exportFiles.offersIncremental() ? MODE_INCREMENTAL : MODE_FULL;
                send(error("scope-mismatch", //$NON-NLS-1$
                        "This computer offers " + (this.exportFiles.offersIncremental() //$NON-NLS-1$
                                ? "an incremental" : "a full") + " export.") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .add("offered", offered) //$NON-NLS-1$
                        .build());
                this.failure = new IOException("Receiver requested " + mode + ", prepared is " + offered); //$NON-NLS-1$ //$NON-NLS-2$
                finish(TransferOutcome.SCOPE_MISMATCH);
                return false;
            }
            this.request.complete(incremental);
            return true;
        }

        /** @return List&lt;String> the file names, null if the value is not an array of strings */
        private static List<String> importedNames(final JsonValue value) {
            if (!(value instanceof final JsonArray array)) {
                return null;
            }
            final List<String> names = new ArrayList<>();
            for (final JsonValue element : array) {
                if (!(element instanceof final JsonString name)) {
                    return null;
                }
                names.add(name.getString());
            }
            return names;
        }

        // --- desktop -> phone: manifest and the raw file contents

        private void sendExport(final boolean incremental) {
            final List<ExportFile> files;
            final JsonArrayBuilder list = JSON.createArrayBuilder();
            final List<String> names = new ArrayList<>();
            try {
                files = this.exportFiles.forRequest(incremental);
                for (final ExportFile file : files) {
                    list.add(JSON.createObjectBuilder()
                            .add("name", file.name()) //$NON-NLS-1$
                            .add("size", Files.size(file.path())) //$NON-NLS-1$
                            .add("sha256", sha256(file.path()))); //$NON-NLS-1$
                    names.add(file.name());
                }
            } catch (final IOException exc) {
                sendError("no-export", "No Relations export available."); //$NON-NLS-1$ //$NON-NLS-2$
                fail(TransferOutcome.PROTOCOL_ERROR, exc);
                return;
            }
            this.sentNames = List.copyOf(names);
            send(JSON.createObjectBuilder()
                    .add("type", "manifest") //$NON-NLS-1$ //$NON-NLS-2$
                    .add("files", list) //$NON-NLS-1$
                    .build());
            try {
                for (final ExportFile file : files) {
                    writeFile(file.path());
                }
            } catch (final IOException exc) {
                // the manifest is out, so the stream cannot carry an error frame any more
                fail(TransferOutcome.INTERRUPTED, exc);
            }
        }

        private static String sha256(final Path path) throws IOException {
            try (DigestInputStream source = new DigestInputStream(Files.newInputStream(path),
                    MessageDigest.getInstance("SHA-256"))) { //$NON-NLS-1$
                source.transferTo(OutputStream.nullOutputStream());
                return HexFormat.of().formatHex(source.getMessageDigest().digest());
            } catch (final NoSuchAlgorithmException exc) {
                throw new IllegalStateException(exc);
            }
        }

        /** Writes the file in chunks, each only after the previous one has been written, so
         * that at most one chunk is buffered. Runs off the event loop. */
        private void writeFile(final Path path) throws IOException {
            try (InputStream source = Files.newInputStream(path)) {
                final byte[] chunk = new byte[CHUNK_SIZE];
                int read;
                while ((read = source.read(chunk)) > 0) {
                    if (this.outcome.isDone()) {
                        return;
                    }
                    write(Unpooled.copiedBuffer(chunk, 0, read));
                }
            }
        }

        private void write(final ByteBuf data) throws IOException {
            final Channel target = this.channel;
            if (target == null) {
                this.stream.writeAndFlush(data);
                return;
            }
            final ChannelFuture written = target.writeAndFlush(data).awaitUninterruptibly();
            if (!written.isSuccess()) {
                throw new IOException("Writing to the receiver failed", written.cause()); //$NON-NLS-1$
            }
        }

        // --- helpers

        private void send(final JsonObject json) {
            this.stream.writeAndFlush(frame(json));
        }

        private void sendError(final String code, final String message) {
            send(error(code, message).build());
        }

        private void protocolError(final String message) {
            sendError("protocol", message); //$NON-NLS-1$
            this.failure = new IOException("Protocol error: " + message); //$NON-NLS-1$
            finish(TransferOutcome.PROTOCOL_ERROR);
        }

        private void fail(final TransferOutcome result, final Throwable cause) {
            if (!this.outcome.isDone()) {
                this.failure = cause;
            }
            finish(result);
        }

        /** The single exit: the first outcome wins; it withdraws a pending approval and ends
         * the stream. */
        private void finish(final TransferOutcome result) {
            if (!this.outcome.complete(result)) {
                return;
            }
            final CompletableFuture<Boolean> asked = this.approval;
            if (asked != null) {
                asked.cancel(false);
            }
            this.request.cancel(false);
            this.stream.close();
        }
    }

    // ===

    public record ExportFile(String name, Path path) {
    }

    /** The files the session offers. */
    interface ExportFiles {
        /** @return boolean <code>true</code> if the user prepared an incremental export; a request for the other
         *         scope is refused */
        boolean offersIncremental();

        /** @param incremental boolean the phone's mode, always equal to {@link #offersIncremental()}
         * @return List of files: for full exactly one named "relations_all.zip", for incremental zero or more named
         *         "relations_delta_…", oldest first */
        List<ExportFile> forRequest(boolean incremental) throws IOException;

        /** Called with the names the phone imported successfully, e.g. to delete those increments. */
        void imported(List<String> names);
    }

    /** Observes the transfers of one session. */
    interface TransferObserver {
        /** A device opened the protocol stream; the session's outcome follows. */
        void started(Session session);

        /** A device opened the protocol stream, but no session could be set up for it.
         *
         * @param remotePeerId String the authenticated identity of the device
         * @param cause {@link Throwable} what went wrong */
        void failedToStart(String remotePeerId, Throwable cause);
    }

}
