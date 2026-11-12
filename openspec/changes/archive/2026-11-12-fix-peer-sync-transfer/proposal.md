# Proposal

## Why

Export to Peer does not work end to end. The uncommitted rework of the libp2p sender moves
from the binary `/relations/export/1.0.0` protocol to a JSON-framed, receiver-driven
protocol, `/relations/sync/1.0.0`, to match the Android companion application. That rework
is only partly finished. As it stands, the first connection fails before any exchange,
because no JSON implementation can be loaded inside OSGi. Past that point several defects
would still break or undermine a transfer:

- **No JSON provider at runtime.** `ExportSender.Session` calls `JsonProvider.provider()` in
  a static initializer. Parsson registers itself only through the OSGi ServiceLoader
  Mediator, and every capability involved is `resolution:=optional`. With
  `autoIncludeRequirements`, Tycho therefore includes `jakarta.json-api` but not Parsson.
  Even if Parsson were present, SPI Fly is neither declared nor started, and Parsson does
  not export `org.eclipse.parsson`. The lookup throws on the Netty thread and the stream is
  reset with nothing shown to the user.
- **The scope can disagree with the phone's request.** The user picks full or incremental
  on the desktop and exactly one file is prepared. `RelationsExportFiles.forRequest()`
  ignores the mode in the phone's `request` and sends that file anyway, so a phone that
  asks for a full export can receive a delta. The change log is then cleared all the same.
- **Approval cannot be compared with the phone.** The protocol pairs the two devices with a
  6-digit confirmation code that both screens show. `confirmationCode()` exists but is never
  called, and the desktop prompt shows a raw peer id instead.
- **Stale approval prompts.** When the phone disconnects, sends `error` or finishes, the
  pending approval is not cancelled. The prompt stays open, and approving it later writes
  to a dead stream.
- **Completion is accepted without checks.** Any `result` frame counts as `COMPLETED`, even
  when its `imported` list does not name the files that were sent. The stream is never
  closed afterwards.
- **No integrity data or version error.** The manifest carries no digest, so the
  `DIGEST_MISMATCH` outcome can never occur and the spec's integrity requirement is unmet.
  A wrong protocol version is reported as a generic protocol error, which leaves task 11.5
  of `add-export-to-peer` blocked.
- **Contract drift.** `PROTOCOL.md`, the class Javadoc and the provider Javadoc still
  describe `/relations/export/1.0.0` and `_relations-export._udp.local.`. The code now uses
  `/relations/sync/1.0.0` and `_relations-sync._udp.local.`. The code cites
  `docs/peer-to-peer-protocol.md`, which is not in this repository. The Android side
  therefore has no single authoritative contract to implement against.
- **Unbounded buffering.** `writeFile()` queues every 64 KiB chunk without waiting for
  earlier writes to finish, so the whole export ends up in Netty buffers.

## What Changes

- Make the JSON implementation load reliably under OSGi. Vendor `jakarta.json-api` and
  `parsson` on the peer bundle's `Bundle-ClassPath`, like every other libp2p dependency, and
  create the provider directly rather than through `ServiceLoader`. Remove the two
  target-platform Maven entries and the `jakarta.json` `Import-Package` lines.
- Keep the scope decision on the desktop, as the user chose. When a phone requests a mode
  other than the one the user prepared, the sender refuses with an `error` frame naming
  both modes. Nothing is sent and the change log is left unchanged.
- Show the 6-digit confirmation code in the desktop approval prompt, derived from both
  authenticated peer ids, alongside the connecting device's peer identity.
  `IPeerConnectionApproval` gains an `approve` overload that carries the code, as a default
  method, so the change stays backward compatible. `PeerTransferOutcome` gains
  `VERSION_MISMATCH` and `SCOPE_MISMATCH`, and `org.elbe.relations.services` moves to the
  minor version 2.2.0.
- Cancel the pending approval, and close its prompt, as soon as a session ends for any
  reason. A late approval never writes to a stream.
- Report `COMPLETED` only when `result.imported` names every file in the manifest. Close
  the stream after the result or after an error.
- Add a per-file `sha256` to the manifest. A new `VERSION_MISMATCH` outcome, raised from
  the in-band `protocol` field, is shown to the user and written to the log.
- Rewrite `PROTOCOL.md` as the authoritative `/relations/sync/1.0.0` contract: service tag,
  frames, sequence, confirmation code, error cases. Fix the stale Javadoc and point code
  comments at `PROTOCOL.md`.
- Write chunks with back-pressure, so the next chunk is written only after the previous
  write completes.
- Remove the dead leftovers of the binary protocol: the magic constants, the duplicate
  `CHUNK_SIZE`, `Scope.getWireValue`, the identity `toScope` mapping and the unused
  `getListenAddresses`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `peer-export`: the approval shows a confirmation code; the phone's requested mode must
  match the prepared scope; integrity comes from a manifest digest; a version mismatch is
  detected in band; the documented protocol becomes `/relations/sync/1.0.0`.

  This capability exists so far only as the delta in the unarchived change
  `add-export-to-peer`. That change must be archived first, or this delta folded into it.

## Impact

**`org.elbe.relations.peer.libp2p`**
- `ExportSender.java`: session state machine, approval cancellation, result validation,
  digest, version check, back-pressure, dead code.
- `Libp2pPeerTransferProvider.java`: scope matching in `RelationsExportFiles`, outcome
  mapping, confirmation code passed to the approval.
- `TransferOutcome.java`: `VERSION_MISMATCH` and `SCOPE_MISMATCH`.
- `META-INF/MANIFEST.MF`, `build.properties`, `libs/`: two more vendored jars (35 in all),
  and the `jakarta.json` imports removed.
- `PROTOCOL.md`: rewritten.

**`org.elbe.relations`**
- `services/IPeerConnectionApproval.java`, `services/PeerTransferOutcome.java`,
  `services/PeerExport.java` (`getWireValue` removed); the exported package version rises
  to 2.2.0.
- The approval prompt used by `ExportToPeerAction`, `ExportToPeerAction` itself (status
  text for the new outcomes), and `RelationsMessages.properties` plus its `_de` sibling.

**`target-platform/target-platform.target`**
- The uncommitted `jakarta.json-api` and `parsson` entries are removed again. This returns
  the file to its committed state, but it is still a target-platform edit and needs a cold
  resolve.

**Not affected**
- The data layer, the schema, cloud providers, and discovery and host lifecycle in
  `Libp2pTransferHost`.

**External**
- The Android companion application must agree on three things: the confirmation-code
  derivation, the added `sha256` manifest field, and the `error` frame for a mode or
  version mismatch. `PROTOCOL.md` becomes the document both sides are checked against.
