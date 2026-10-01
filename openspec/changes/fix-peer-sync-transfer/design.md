# Design

## Context

See `proposal.md` (Why) for the defects. This change builds on the unarchived
`add-export-to-peer`, whose design still holds for everything not named here: the libp2p
host, TCP + Noise + mplex, mDNS discovery by address only, the persisted identity, the thin
handler, and a change log shared with cloud export.

State of the working tree this design starts from:

- `ExportSender` already implements a JSON-framed, receiver-driven exchange under
  `/relations/sync/1.0.0`. Each frame is a 4-byte big-endian length followed by UTF-8 JSON,
  capped at 65 536 bytes. The desktop sends `hello`. The phone sends `request` with a
  `protocol` field and a `mode`. The desktop replies with `manifest`, then the raw file
  bytes, back to back. The phone ends with `result` (`imported`: names) or `error`.
- `ExportOffer` and `TransferScope` are already deleted. `PeerExport.Scope` carries a
  `wireValue` left over from the binary header.
- The target platform gained Maven entries for `jakarta.json-api` 2.1.3 and `parsson`
  1.1.6. Neither is listed in the feature, so the product contains `jakarta.json-api` only
  because `autoIncludeRequirements` pulls it in. Parsson is not pulled in, because nothing
  requires it except as an optional capability.
- The existing product (build of 2026-09-24) does contain
  `org.apache.aries.spifly.dynamic.bundle`, arriving transitively. Nothing declares it or
  starts it.
- `docs/peer-to-peer-protocol.md`, cited in comments, is not in this repository.
  `PROTOCOL.md` in the bundle still documents the binary protocol.

## Goals / Non-Goals

**Goals:**

- The first connection from a receiver completes the exchange inside the running product,
  not only in a plain-JVM test.
- The sender's behaviour is fully determined by `PROTOCOL.md`, and the reference receiver is
  rebuilt from that document to prove it.

**Non-Goals:**

- Changing the host, the transport stack or discovery. `Libp2pTransferHost` keeps its
  `_relations-sync._udp.local.` tag. Only documentation around it changes.
- Multiple delta files per transfer. `forRequest(true)` keeps returning the single delta
  prepared on the desktop. The protocol already allows zero or more, so this is a
  limitation of the sender, not of the contract.
- Implementing `ExportFiles.imported()` to delete increments. There are no stored
  increments on the desktop; the prepared file is a temp file deleted by
  `ExportToPeerAction`.
- The Android application.

## Decisions

### Vendor JSON-P on `Bundle-ClassPath` and construct the provider directly

Options considered:

| Option | Problem |
|---|---|
| Keep the target-platform bundles and add Parsson + SPI Fly to the feature, started at level 2 | Needs three OSGi pieces wired correctly: Parsson's `osgi.serviceloader` capability, SPI Fly's processor for the API's `ServiceLoader.load`, and a start level. Every requirement is `resolution:=optional`, so a mistake resolves cleanly and fails at the first connection. It also adds a target-platform dependency. |
| Import `org.eclipse.parsson` and call `new JsonProviderImpl()` | Parsson exports only `org.eclipse.parsson.api`, so the implementation package is not importable. |
| **Vendor both jars on `Bundle-ClassPath` and call `new org.eclipse.parsson.JsonProviderImpl()`** | Two more jars (~250 KB in total). |
| Drop JSON-P and hand-roll the small frames | Parsing JSON from a foreign implementation by hand is where interop bugs come from. |

The third option is chosen. It follows the convention already used for all 33 libp2p
dependencies (see `add-export-to-peer` design, 1.3): the bundle's own classloader resolves
everything, and no OSGi service lookup is involved. The provider is created once, in a
`static final` field of `ExportSender`, and never through `JsonProvider.provider()`.

The `jakarta.json` lines in `Import-Package` are removed. Otherwise the bundle would wire
to a platform copy and mix classloaders. The two target-platform entries are removed
because nothing else uses them.

An initializer failure must never again vanish on a Netty thread.
`Handler.onStartResponder` catches `Throwable` around session creation, logs it, and
reports it through the listener as `PROTOCOL_ERROR` before resetting the stream (spec:
"Failure on the first exchange").

### The desktop's scope is authoritative; a mismatch is an error frame

This decision is the user's. `RelationsExportFiles` knows the `PeerExport.Scope` the user
prepared. On `request`, the session compares the requested `mode` with that scope. If they
differ it sends:

```json
{"type":"error","code":"scope-mismatch","message":"…","offered":"incremental"}
```

It then finishes with `SCOPE_MISMATCH` and closes the stream. It does this before calling
`forRequest`, so no file is read. `offered` tells the phone which mode to ask for next
time. The session stays open for another connection, as it does after a decline.

Considered: sending what the desktop prepared regardless of the request. This was rejected
because the phone applies a full export and a delta differently. Silently substituting one
for the other is the defect being fixed.

### `error` frames gain a machine-readable `code`

A human-readable `message` alone cannot be mapped to an outcome. Both directions use
`code`, a closed set: `declined`, `busy`, `version-mismatch`, `scope-mismatch`,
`no-export`, `protocol`, `digest-mismatch`, `storage`. The desktop maps the phone's
`digest-mismatch` to `DIGEST_MISMATCH` and `storage` to `STORAGE_ERROR`. Any other code,
or no code, maps to `REJECTED_BY_RECEIVER`. An unknown code is not a protocol error, so the
phone can add codes later.

### Version check in band, before anything else

multistream-select refuses a foreign protocol id without telling the sender anything. That
is why task 11.5 of the earlier change is blocked, and it stays that way for ids other than
`/relations/sync/*`. The one version that can be detected is the `protocol` integer in the
phone's `request`. When it is not `1`, the desktop sends `error` with code
`version-mismatch` and `"supported":1`, then finishes with `VERSION_MISMATCH`, which the
user sees distinctly. The spec's "declares an unsupported protocol version" is worded to
match exactly this.

### Confirmation code from both authenticated ids

`confirmationCode(a, b)` already exists. It sorts the two ids' raw bytes as unsigned,
hashes them with SHA-256, takes the first 4 bytes big-endian modulo 10^6, and pads to 6
digits. Both inputs come from the Noise session: the local id is
`stream.getConnection().secureSession().getLocalId()` and the remote id is
`remotePeerId()`, each as `PeerId.getBytes()`. Taking them from the handshake rather than
from the identity store means the code reflects what was actually authenticated on this
connection.

SPI: `IPeerConnectionApproval` gains
`default CompletableFuture<Boolean> approve(String peerId, String confirmationCode)`, which
delegates to `approve(peerId)`. `PeerApprovalPrompt` overrides it and shows the code
prominently, with the peer id as secondary detail. `ApprovalGate` in the peer bundle
changes to the two-argument form. The exported package moves to 2.2.0, and the peer
bundle's import to `[2.2.0,3.0.0)`. Cloud bundles are unaffected.

### Session end cancels the approval

`Session.finish()` becomes the single exit, and it runs its side effects only once, when
`outcome.complete()` returns `true`. It then:

1. cancels the approval future (`cancel(false)`);
2. closes the stream if it is still open.

`PeerApprovalPrompt` registers `answer.whenComplete(...)`. When the future is cancelled it
closes the open `MessageDialog` through `display.asyncExec`, and a result from
`dialog.open()` after that point is ignored. On the sender side, both continuations check
`outcome.isDone()` before acting, so a late `true` sends nothing.

### Completion requires every sent name

`sendExport` records the names in the manifest. On `result`, `COMPLETED` requires
`imported` to contain every recorded name. Otherwise the session finishes with
`REJECTED_BY_RECEIVER`, and the change log is kept. A `result` that arrives before the
manifest was sent is a protocol error.

### Per-file SHA-256 in the manifest

The digest is computed while the manifest is built, by streaming each file once. It is sent
as `"sha256":"<64 lowercase hex>"`. This adds a field to the manifest; it does not change
the framing. It is added before `1.0.0` is frozen in `PROTOCOL.md`, so the version stays
`1.0.0`.

### Back-pressure

`writeFile` waits for each chunk's `writeAndFlush` future before reading the next chunk, on
the async thread that already runs `sendExport`, never on the event loop. Peak buffering is
then one chunk, not one export. A failed write future ends the session as `INTERRUPTED`.

### `PROTOCOL.md` is the one contract

The file is rewritten for `/relations/sync/1.0.0`. It covers transport, the service tag,
framing, each frame type with its fields, the sequence with a diagram, the
confirmation-code algorithm with a test vector, error codes, and the version and scope
rules. Code comments that cite `docs/peer-to-peer-protocol.md` are changed to cite
`PROTOCOL.md`. The reference receiver from `add-export-to-peer` 3.4 is rebuilt against the
new document; this is the check that the document is sufficient on its own.

## Risks / Trade-offs

- **The Android side may already implement a different draft** → Every extension here is
  additive (`sha256`, `code`, `offered`, `supported`) or already present in the code
  (`confirmationCode`). Share `PROTOCOL.md` with the Android side before freezing it. If the
  phone's draft differs in the confirmation-code derivation, the test vector exposes it at
  once.
- **Two more vendored jars** → Small and pure Java. Recorded in the bundle's list of
  vendored artifacts, in the same way as the existing 33.
- **`secureSession().getLocalId()` accessor name in jvm-libp2p 1.3.7 is from memory** → If
  it differs, fall back to the host's own `PeerId`, which is the same value because the
  host has exactly one identity. Task 2.3 checks this.
- **Refusing a mode mismatch can annoy users** → The error names the offered mode, so the
  phone can explain what happened. The trade-off follows from the user's decision that the
  desktop is authoritative.
- **Removing the target-platform entries is a target-platform edit** → It restores the
  committed state, but it is still verified with a cold resolve, as the project requires.
- **No automated verification** → As before, `mvn clean verify` runs no tests. The checks
  in `tasks.md` state how each item is verified: a plain-JVM test against the reference
  receiver, the product's headless Equinox, or a manual GUI run.

## Migration Plan

- Archive `add-export-to-peer` before this change, so that `peer-export` exists as a main
  spec for this delta to modify. Alternatively, fold this delta into that change before
  archiving it.
- Nothing is stored on disk that needs migrating. The peer identity and the port preference
  are unchanged.
- Rollback: revert the bundle. The services package change is additive, so reverting the
  peer bundle alone leaves the host working.

## Implementation notes

Recorded during apply, where the implementation departs from the decisions above or found
something they did not anticipate.

- **Write futures come from the Netty channel.** `Stream.writeAndFlush` returns `void` in
  jvm-libp2p 1.3.7. A `ChannelInboundHandlerAdapter` pushed before the session captures the
  stream's Netty channel, whose `writeAndFlush` returns a `ChannelFuture`. Only public API is
  used.
- **Early failure for a missing JSON implementation.** The provider is a static field of
  `ExportSender`, so a missing Parsson surfaces as a `LinkageError` in `openSession`. That
  is reported as `PeerSessionException(NOT_STARTED)`, and the user sees "session could not
  be opened" before any device connects.
- **The underlying error reaches the log through the SPI.**
  `IPeerTransferListener.transferEnded(peerId, outcome, cause)` was added as a default
  method alongside the approval overload, still in 2.2.0. `ExportToPeerAction` logs the
  cause. `TransferObserver.failedToStart` carries the cause from `onStartResponder`.
- **Vendored-artifact record.** `VENDORED.md` at the bundle root lists all 35 jars with
  their coordinates and source hosts. The coordinates were read from the jars' own
  `pom.properties` where present.
- **Open: back-pressure versus slow receivers (task 2.7).** Waiting for each 64 KiB write
  bounds the sender's memory: 100 MB is sent with a 32 MB heap. But when the receiver reads
  slowly (30 ms per read, simulated), the receiver fails after ~70 MB with
  `CantDecryptInboundException` / `AEADBadTagException` from Noise, reproduced twice. With
  unthrottled writes, the previous behaviour, the same slow run succeeds but peaks at
  ~240 MB of heap for 100 MB.

  Two alternatives were tried in throwaway variants. A window of 16 writes in flight
  stalled. Throttling on the connection's writability succeeded under a slow reader but did
  not bound memory: a 32 MB heap hung. The cause appears to lie in jvm-libp2p's mplex/Noise
  write path, and it needs a decision (see the apply summary). With the per-chunk wait that
  is implemented, a 20 MB transfer to a receiver reading at 20 ms per read did complete.
