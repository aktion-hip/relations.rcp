# Spec Delta

## Purpose

Lets a user transfer a snapshot of their knowledge base from Relations directly to a
companion application on their own mobile device over an encrypted peer-to-peer
connection on the local network, without a server, a storage account, or a cable.

## ADDED Requirements

### Requirement: Export to Peer action is reachable from the Utility menu

The application SHALL offer an "Export to Peer …" action in the Utility menu, positioned
after the existing "Export to Cloud …" action. The action SHALL be available in every
supported interface language.

#### Scenario: Action is present

- **WHEN** the user opens the Utility menu
- **THEN** an "Export to Peer …" entry is shown after "Export to Cloud …" and before "Preferences"

#### Scenario: Peer networking cannot start

- **WHEN** the user invokes "Export to Peer …" and the application cannot begin listening for peers
- **THEN** the user is told why, no transfer session is opened, and no data is exported

### Requirement: The application has a stable peer identity

The application SHALL hold its own peer identity and SHALL reuse it across restarts, so
that a receiving device can recognise the same installation on a later transfer. The user
SHALL be able to see their own peer identity, and SHALL be able to configure the network
port used for incoming connections. The port SHALL default to **9042**.

#### Scenario: Identity survives restart

- **WHEN** the user opens a transfer session, restarts the application, and opens another
- **THEN** the peer identity reported in both sessions is the same

#### Scenario: Default port

- **WHEN** the user has not changed the port setting
- **THEN** the application listens on port 9042

#### Scenario: Configured port is in use

- **WHEN** the configured port cannot be bound because another process holds it
- **THEN** the user is told the port is unavailable and no transfer session is opened

### Requirement: A transfer is a live session on the local network

Export to Peer SHALL open a transfer session that exists only while the sending
application keeps it open, and that is reachable by devices on the same local network.
The application SHALL state that the session is live and that the receiving device must
connect while it lasts. When the session is closed, or the application exits, the export
SHALL no longer be obtainable.

#### Scenario: Receiving device connects during the session

- **WHEN** the receiving application connects to the advertised address while the session is open
- **THEN** the transfer proceeds

#### Scenario: Receiving device connects after the session ends

- **WHEN** the user closes the session or Relations exits
- **AND** the receiving application then attempts to connect to the same address
- **THEN** the connection does not succeed

#### Scenario: Session nature is stated

- **WHEN** a transfer session is opened
- **THEN** the application states that the session is live, is limited to the local network, and ends when it is closed

### Requirement: The session is discoverable on the local network and by address

While a session is open the application SHALL advertise itself for discovery on the local
network, so a receiving application on the same network can find it without the user
typing an address. The application SHALL additionally present its address so the receiving
application can be pointed at it directly when discovery is unavailable.

#### Scenario: Session is discoverable

- **WHEN** a session is open and the receiving device is on the same local network
- **THEN** the receiving application can locate the session without the user entering an address

#### Scenario: Address is available as a fallback

- **WHEN** discovery does not find the session
- **THEN** the address shown by Relations can be used to connect directly

#### Scenario: Discovered identity is not authoritative

- **WHEN** a session is located through local-network discovery
- **THEN** the identity carried in the announcement is treated only as an unverified hint
- **AND** the identity used for approval and for the transfer is the one established by the encrypted handshake

### Requirement: The sender authorizes each incoming connection

Before any exported content is sent, the application SHALL present the identity of the
connecting device to the user and SHALL require explicit approval. Content SHALL NOT be
transmitted to a device the user has not approved for that session.

#### Scenario: User approves a device

- **WHEN** a device connects to an open transfer session
- **THEN** the user is shown the connecting device's identity and asked to approve
- **AND** the content is sent only after approval

#### Scenario: User declines a device

- **WHEN** the user declines the connecting device
- **THEN** no content is sent and the session remains open for a further connection

### Requirement: The peer address is presented for sharing

On opening a session the application SHALL present the address at which it can be
reached, including its peer identity, in a form the user can copy in one action. The
presentation SHALL state that the address is valid only for the duration of the session.

#### Scenario: Address is shown and can be copied

- **WHEN** a transfer session is opened
- **THEN** the peer address is displayed and can be copied to the clipboard in one action

#### Scenario: Address is qualified as session-scoped

- **WHEN** the peer address is displayed
- **THEN** the display states that it works only while the session remains open

### Requirement: User chooses export scope before the session opens

The application SHALL let the user choose between sending the complete data set and
sending only the changes recorded since the last export. The incremental choice SHALL be
offered only when recorded changes exist, and SHALL otherwise be unavailable with the
complete export preselected.

#### Scenario: Recorded changes exist

- **WHEN** the user invokes "Export to Peer …" and the change log contains entries
- **THEN** both scope choices are offered and the incremental choice is preselected

#### Scenario: No recorded changes exist

- **WHEN** the user invokes "Export to Peer …" and the change log is empty
- **THEN** the incremental choice is unavailable and the complete export is preselected

### Requirement: The change log is shared with cloud export and the user is told

Peer export and cloud export SHALL consume the same single change log. Before an
incremental export, the application SHALL state that the export consumes the change log
shared with the other destination, so that a subsequent incremental export to the other
destination will contain no changes.

#### Scenario: Warning is shown for incremental scope

- **WHEN** the user selects the incremental scope in either the peer or the cloud export dialog
- **THEN** the dialog states that the change log is shared between peer and cloud export and will be consumed

#### Scenario: Incremental peer export empties the cloud delta

- **WHEN** the user completes an incremental peer transfer successfully
- **AND** then invokes cloud export without making further changes
- **THEN** the incremental choice is unavailable because the change log is empty

### Requirement: The change log is cleared only after an acknowledged transfer

The application SHALL clear the change log only once the receiving application has
acknowledged delivery of the export in full. When no device connects, when the transfer is
interrupted, or when the user closes the session first, the change log SHALL be left
intact so that the unexported changes remain available for a later attempt.

#### Scenario: No device connects

- **WHEN** the user opens an incremental transfer session and closes it without any device connecting
- **THEN** the change log still contains the entries it held before
- **AND** a subsequent incremental export offers those same changes

#### Scenario: Transfer is interrupted

- **WHEN** a transfer begins and the connection is lost before delivery has been acknowledged
- **THEN** the transfer is reported as failed and the change log is left intact

### Requirement: The user is warned what a transfer session exposes

Before opening a session the application SHALL state that any device able to reach the
session on the local network may request the export, that the export contains the user's
knowledge base in full, and that each connection must be approved. The user SHALL be able
to abandon the export at that point.

#### Scenario: Warning precedes the session

- **WHEN** the user confirms an export to a peer
- **THEN** the application states what the session exposes and that each connection requires approval
- **AND** the session opens only on explicit confirmation

#### Scenario: User declines

- **WHEN** the user declines at the warning
- **THEN** no session is opened and the change log is left intact

### Requirement: Failures are reported with a distinguishable cause

When a transfer cannot be completed, the application SHALL report the failure to the user
and SHALL distinguish being unable to open a session, no device having connected, and a
transfer that started but did not finish. Diagnostic detail SHALL be written to the
application log.

#### Scenario: Session cannot be opened

- **WHEN** the application cannot listen for peers
- **THEN** the user is told a session could not be opened, and the underlying error is written to the log

#### Scenario: Transfer starts but does not complete

- **WHEN** a device connects and is approved but the transfer does not finish
- **THEN** the user is told the transfer was interrupted, distinctly from no device having connected
- **AND** the underlying error is written to the log

### Requirement: The transfer protocol is specified for independent implementation

Because the receiving application is a separate product on a different platform, the
transfer protocol SHALL be specified in enough detail to be implemented independently,
and SHALL carry an explicit version identifier so sender and receiver can detect a
mismatch. A connection requesting a protocol version the application does not support
SHALL be refused with a distinguishable error rather than transferring content.

#### Scenario: Protocol is documented

- **WHEN** the change is delivered
- **THEN** a specification of the protocol identifier, framing, header fields and acknowledgement exists alongside the implementation

#### Scenario: Version mismatch is refused

- **WHEN** a connecting application requests an unsupported protocol version
- **THEN** no content is transferred and both the user and the log record a version mismatch

### Requirement: Transferred content has the same format as cloud export

The content sent to the receiving device SHALL be the same zipped XML export produced for
cloud export, so that the receiving application can rely on a format the project already
produces and documents elsewhere.

#### Scenario: Transferred export matches the cloud export format

- **WHEN** a complete export is transferred
- **THEN** the delivered file is byte-equivalent in format to the file the cloud export would have uploaded for the same data

#### Scenario: Integrity is verifiable by the receiver

- **WHEN** an export is transferred
- **THEN** the transfer carries a digest of the payload that the receiving application can use to verify what it received

### Requirement: The connection is encrypted and the peer is authenticated

Content SHALL be transferred only over a connection that is encrypted and in which the
remote device's identity has been cryptographically established, so that the identity
shown to the user for approval is the identity that receives the data.

#### Scenario: Identity shown matches identity served

- **WHEN** the user approves a connecting device
- **THEN** the content is delivered only to the device whose established identity was shown

### Requirement: All new user-facing text is available in English and German

Every label, message, warning and error string introduced by this capability SHALL be
provided in both English and German, consistent with the rest of the application.

#### Scenario: German interface

- **WHEN** the application runs with the German interface language
- **THEN** the menu entry, preference page, dialogs, session display, approval prompt and error messages introduced by this capability are shown in German
