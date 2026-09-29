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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import io.libp2p.core.PeerId;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.KeyType;
import io.libp2p.core.crypto.PrivKey;

import kotlin.Pair;

/** Holds this installation's libp2p identity.
 *
 * <p>
 * The identity is generated once and kept, so that an address the user shared keeps
 * identifying the same installation after a restart. It identifies the installation, not
 * the person, and is not tied to any account.
 * </p>
 *
 * @author lbenno */
public class PeerIdentityStore {

    private static final Set<PosixFilePermission> OWNER_ONLY = PosixFilePermissions.fromString("rw-------"); //$NON-NLS-1$

    private final Path keyFile;

    /** PeerIdentityStore constructor.
     *
     * @param keyFile {@link Path} the file holding the marshalled private key, normally
     *            beside the application preferences */
    public PeerIdentityStore(final Path keyFile) {
        this.keyFile = keyFile;
    }

    /** Returns the stored identity, creating and persisting one on first use.
     *
     * @return {@link PrivKey} this installation's private key
     * @throws IOException if the key cannot be read or written */
    public PrivKey loadOrCreate() throws IOException {
        if (Files.exists(this.keyFile)) {
            return KeyKt.unmarshalPrivateKey(Files.readAllBytes(this.keyFile));
        }
        final Pair<PrivKey, ?> generated = KeyKt.generateKeyPair(KeyType.ED25519);
        final PrivKey privateKey = generated.getFirst();
        write(KeyKt.marshalPrivateKey(privateKey));
        return privateKey;
    }

    /** @return {@link PeerId} the peer identity derived from the stored key
     * @throws IOException if the key cannot be read or written */
    public PeerId getPeerId() throws IOException {
        return PeerId.fromPubKey(loadOrCreate().publicKey());
    }

    private void write(final byte[] marshalled) throws IOException {
        final Path parent = this.keyFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(this.keyFile, marshalled);
        restrictToOwner();
    }

    /** The file is a private key, so keep it unreadable by other users where the file
     * system supports it. Windows file systems do not, and that is not an error. */
    private void restrictToOwner() {
        try {
            Files.setPosixFilePermissions(this.keyFile, OWNER_ONLY);
        }
        catch (final UnsupportedOperationException | IOException exc) {
            // POSIX permissions are unavailable on this file system
        }
    }
}
