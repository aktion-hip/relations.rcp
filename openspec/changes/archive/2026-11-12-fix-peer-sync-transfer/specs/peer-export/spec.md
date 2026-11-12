# Spec Delta

## MODIFIED Requirements

### Requirement: The sender authorizes each incoming connection

Before any exported content is sent, the application SHALL present the identity of the
connecting device to the user together with a short confirmation code, and SHALL require
explicit approval. The confirmation code SHALL be derived from the cryptographically
established identities of both devices, so that the same code appears on the receiving
device and the user can check that they are approving the device in their hand. Content
SHALL NOT be transmitted to a device the user has not approved for that session. When a
connection ends before the user has decided, the approval request SHALL be withdrawn, and
a later decision SHALL NOT cause content to be sent.

#### Scenario: User approves a device

- **WHEN** a device connects to an open transfer session
- **THEN** the user is shown the connecting device's identity and a six-digit confirmation code, and is asked to approve
- **AND** the content is sent only after approval

#### Scenario: Confirmation code matches the receiving device

- **WHEN** a device connects and both the desktop and the receiving device display a confirmation code
- **THEN** the two codes are identical
- **AND** a different device connecting to the same session produces a different code

#### Scenario: User declines a device

- **WHEN** the user declines the connecting device
- **THEN** no content is sent and the session remains open for a further connection

#### Scenario: Device disconnects while approval is pending

- **WHEN** the connecting device disconnects, or reports an error, before the user has approved or declined
- **THEN** the approval request is withdrawn from the screen
- **AND** no content is sent to any device as a result of that request

### Requirement: User chooses export scope before the session opens

The application SHALL let the user choose between sending the complete data set and
sending only the changes recorded since the last export. The incremental choice SHALL be
offered only when recorded changes exist, and SHALL otherwise be unavailable with the
complete export preselected. The scope chosen by the user is authoritative for the
session: when a receiving device requests a different scope, the application SHALL refuse
that request with an error that names both the prepared and the requested scope, SHALL send
no content, and SHALL leave the change log intact.

#### Scenario: Recorded changes exist

- **WHEN** the user invokes "Export to Peer …" and the change log contains entries
- **THEN** both scope choices are offered and the incremental choice is preselected

#### Scenario: No recorded changes exist

- **WHEN** the user invokes "Export to Peer …" and the change log is empty
- **THEN** the incremental choice is unavailable and the complete export is preselected

#### Scenario: Receiving device requests the prepared scope

- **WHEN** the user prepared a complete export and the approved device requests a complete export
- **THEN** the complete export is sent

#### Scenario: Receiving device requests a different scope

- **WHEN** the user prepared an incremental export and the approved device requests a complete export
- **THEN** no content is sent and the receiving device is told that the desktop offers an incremental export
- **AND** the user is told the request did not match the prepared scope
- **AND** the change log still contains the entries it held before

### Requirement: The change log is cleared only after an acknowledged transfer

The application SHALL clear the change log only once the receiving application has
acknowledged delivery of the export in full, meaning that its acknowledgement names every
file the application sent in that transfer. When no device connects, when the transfer is
interrupted, when the acknowledgement omits any sent file, or when the user closes the
session first, the change log SHALL be left intact so that the unexported changes remain
available for a later attempt.

#### Scenario: No device connects

- **WHEN** the user opens an incremental transfer session and closes it without any device connecting
- **THEN** the change log still contains the entries it held before
- **AND** a subsequent incremental export offers those same changes

#### Scenario: Transfer is interrupted

- **WHEN** a transfer begins and the connection is lost before delivery has been acknowledged
- **THEN** the transfer is reported as failed and the change log is left intact

#### Scenario: Acknowledgement is incomplete

- **WHEN** the receiving device acknowledges the transfer but does not name every file that was sent
- **THEN** the transfer is reported as not completed and the change log is left intact

### Requirement: Failures are reported with a distinguishable cause

When a transfer cannot be completed, the application SHALL report the failure to the user
and SHALL distinguish being unable to open a session, no device having connected, a
transfer that started but did not finish, a protocol version mismatch, and a scope
mismatch. Diagnostic detail SHALL be written to the application log. A failure SHALL be
reported even when it occurs on the first exchange of a connection.

#### Scenario: Session cannot be opened

- **WHEN** the application cannot listen for peers
- **THEN** the user is told a session could not be opened, and the underlying error is written to the log

#### Scenario: Transfer starts but does not complete

- **WHEN** a device connects and is approved but the transfer does not finish
- **THEN** the user is told the transfer was interrupted, distinctly from no device having connected
- **AND** the underlying error is written to the log

#### Scenario: Failure on the first exchange

- **WHEN** a device connects and the application cannot take part in the exchange at all
- **THEN** the user is told the transfer failed and the underlying error is written to the log, rather than the connection ending silently

### Requirement: The transfer protocol is specified for independent implementation

Because the receiving application is a separate product on a different platform, the
transfer protocol SHALL be specified in enough detail to be implemented independently,
and SHALL carry an explicit version identifier so sender and receiver can detect a
mismatch. The specification kept alongside the implementation SHALL be the single
authoritative description of the protocol, and the implementation SHALL match it in its
protocol identifier, discovery service name, message types and sequence. A connection that
declares a protocol version the application does not support SHALL be refused with a
distinguishable error rather than transferring content.

#### Scenario: Protocol is documented

- **WHEN** the change is delivered
- **THEN** a specification of the protocol identifier, discovery service name, message framing, message types, sequence, confirmation code and error cases exists alongside the implementation
- **AND** the protocol identifier and discovery service name in that specification are the ones the application uses

#### Scenario: Version mismatch is refused

- **WHEN** a connecting application declares an unsupported protocol version
- **THEN** no content is transferred, the connecting application receives an error naming the supported version, and both the user and the log record a version mismatch

### Requirement: Transferred content has the same format as cloud export

The content sent to the receiving device SHALL be the same zipped XML export produced for
cloud export, so that the receiving application can rely on a format the project already
produces and documents elsewhere. Before sending content, the application SHALL announce
each file's name, size and SHA-256 digest, so that the receiving application can verify
every file it received.

#### Scenario: Transferred export matches the cloud export format

- **WHEN** a complete export is transferred
- **THEN** the delivered file is byte-equivalent in format to the file the cloud export would have uploaded for the same data

#### Scenario: Integrity is verifiable by the receiver

- **WHEN** an export is transferred
- **THEN** the announcement preceding the content carries a SHA-256 digest for each file that the receiving application can use to verify what it received

#### Scenario: Receiver reports a digest mismatch

- **WHEN** the receiving application reports that a received file does not match its announced digest
- **THEN** the transfer is reported to the user as failed because of an integrity error and the change log is left intact
