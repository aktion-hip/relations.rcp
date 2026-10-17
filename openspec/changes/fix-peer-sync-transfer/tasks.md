# Tasks

Note on verification: `mvn clean verify` compiles but runs no tests (`tests/` is outside the
reactor and there is no CI). Each task names its actual check. "Headless Equinox" means the
framework assembled from the product's `plugins/` directory, as used in `add-export-to-peer`
4.2, 4.3 and 10.2. "Reference receiver" means the command-line client from
`add-export-to-peer` 3.4, rebuilt in group 5.

Group 1 is a gate. It fixes the defect that stops every connection. Confirm it inside OSGi
before relying on any later check.

## 1. JSON provider loads inside OSGi

- [x] 1.1 Download `jakarta.json-api-2.1.3.jar` and `parsson-1.1.6.jar` from Maven Central into `bundles/org.elbe.relations.peer.libp2p/libs/`, add both to `Bundle-ClassPath` and `build.properties`, and add them to the bundle's record of vendored artifacts with their source host; verify the built bundle jar contains 35 jars under `libs/`
  - Checked: the built bundle jar has 35 jars under `libs/`; both checksums match Maven Central. The record is the new `VENDORED.md` at the bundle root (none existed before).
- [x] 1.2 Remove the three `jakarta.json*` lines from the peer bundle's `Import-Package` and the two uncommitted `jakarta.json-api` / `parsson` entries from `target-platform/target-platform.target`; verify `git diff --ignore-cr-at-eol target-platform/` is empty and a **cold** resolve from an empty local repository succeeds
  - Checked: `git diff --ignore-cr-at-eol target-platform/` is empty (CRLF-only churn from the working copy remains), and `mvn clean verify` with an empty `-Dmaven.repo.local` resolved the target definition and built all 26 modules.
- [x] 1.3 Replace `JsonProvider.provider()` in `ExportSender.Session` with a `static final` instance created by `new org.eclipse.parsson.JsonProviderImpl()`; verify in the headless Equinox that opening a session and connecting with the reference receiver gets a `hello` frame, where the current build resets the stream
  - Checked in a headless Equinox built from the product's `plugins/`: the provider component is SATISFIED, the receiver gets `hello`, and a full transfer completes (`OsgiCheck`). The build before this change was not re-run for comparison.
- [x] 1.4 In `Handler.onStartResponder`, catch `Throwable` around session creation, log it, and report the transfer to the listener as `PROTOCOL_ERROR` before resetting; verify with a temporary fault injected in the `Session` constructor that the listener receives `transferStarted` and `transferEnded(PROTOCOL_ERROR)`, then remove the injection
  - Checked with a fault injected through a throwing `TransferObserver.started` in the harness, so no source edit had to be reverted: the listener got `transferStarted` then `transferEnded(PROTOCOL_ERROR)` with the cause. Additionally, `openSession` now turns a `LinkageError` into `PeerSessionException(NOT_STARTED)`, so a missing JSON implementation fails when the session opens.

## 2. Session state machine in `ExportSender`

- [x] 2.1 Make `finish()` the single exit: it acts only if `outcome.complete()` returns true, then cancels the approval future and closes the stream; guard both approval continuations with `outcome.isDone()`; verify with the reference receiver that disconnecting while approval is pending ends the session as `INTERRUPTED` and that approving afterwards writes nothing (no bytes arrive on a second, idle stream to the same session)
  - Checked: disconnect while pending gives `INTERRUPTED`, the approval future is cancelled, and completing it afterwards produces no further outcome; the session then serves the next device. The late-approval check observes the cancelled future and the absence of any outcome, rather than bytes on a second stream.
- [x] 2.2 Add `error.code` in both directions, using the closed set from design.md; map the phone's `digest-mismatch` to `DIGEST_MISMATCH`, `storage` to `STORAGE_ERROR`, and anything else to `REJECTED_BY_RECEIVER`; verify each mapping by having the reference receiver send each code
  - Checked: `storage`, `digest-mismatch`, a corrupted payload and an unknown code map as specified.
- [x] 2.3 Derive the confirmation code from `secureSession()` local and remote peer ids (falling back to the host's own `PeerId` if the accessor differs in 1.3.7) and pass it to the approval gate; change `ApprovalGate` to `approve(peerId, confirmationCode)`; verify the code the desktop receives equals the code the reference receiver computes independently from `PROTOCOL.md`
  - Checked: `secureSession().getLocalId()` exists in 1.3.7 (no fallback needed); in every run the desktop's code equals the receiver's independently computed one.
- [x] 2.4 Check `request.protocol` before anything else; if it is not 1, send `error` with `code: "version-mismatch"` and `"supported": 1`, then finish as `VERSION_MISMATCH`; verify with the reference receiver sending `protocol: 2` that no manifest is sent and the outcome is `VERSION_MISMATCH`
  - Checked: `protocol: 2` gives `VERSION_MISMATCH`, no manifest, and `error` with `supported: 1`.
- [x] 2.5 Compare the requested `mode` with the prepared scope before calling `forRequest`; on mismatch send `error` with `code: "scope-mismatch"` and `"offered"`, then finish as `SCOPE_MISMATCH`; verify both mismatch directions send no manifest, and that the same session then completes for a correctly requesting receiver
  - Checked: both directions refused with the right `offered`; the same session then completes a matching request.
- [x] 2.6 Add a per-file `sha256` (64 lowercase hex characters) to each manifest entry, and remember the sent names; on `result`, finish as `COMPLETED` only if `imported` contains every sent name, otherwise as `REJECTED_BY_RECEIVER`; treat a `result` before the manifest as a protocol error; verify with the reference receiver that the digest matches `sha256sum` of the prepared file, and that a `result` omitting the file does not yield `COMPLETED`
  - Checked: the manifest digest equals the prepared file's SHA-256; a `result` omitting the file gives `REJECTED_BY_RECEIVER`.
- [ ] 2.7 Make `writeFile` wait for each chunk's write future before reading the next chunk, and end the session as `INTERRUPTED` when a write fails; verify a transfer of a ≥50 MB export completes and that heap use during it stays well below the file size (VisualVM or `-Xlog:gc`)
  - Implemented (per-chunk wait, via the stream's Netty channel because `Stream.writeAndFlush` returns no future). A 100 MB transfer completes with a 32 MB sender heap. **Open:** with a slow receiver (30 ms per read) a 100 MB transfer fails after ~70 MB with a Noise `AEADBadTagException` on the receiver; without back-pressure the same run succeeds. See design.md, Implementation notes.
- [x] 2.8 Remove the leftovers of the binary protocol: `MAGIC_HEADER`, `MAGIC_ACK`, `DIGEST_SHA_256`, `ACK_FIXED_LENGTH`, the outer `CHUNK_SIZE`, `PeerExport.Scope.getWireValue`, `toScope`, and the unused `Libp2pTransferHost.getListenAddresses`; verify `mvn clean verify` compiles with no new warnings in the two bundles
  - Checked: the cold `mvn clean verify` compiles; no compiler warnings reference the changed files.

## 3. Provider and SPI

- [x] 3.1 Add `VERSION_MISMATCH` and `SCOPE_MISMATCH` to `TransferOutcome` and `PeerTransferOutcome`, and map them in `Libp2pPeerTransferProvider.toOutcome`; verify the build compiles with the exhaustive `switch` in both places
  - Checked: compiles with exhaustive switches in the provider and in `ExportToPeerAction`.
- [x] 3.2 Add `default CompletableFuture<Boolean> approve(String peerId, String confirmationCode)` to `IPeerConnectionApproval`, delegating to `approve(peerId)`; raise the `org.elbe.relations.services` export to 2.2.0 and the peer bundle's import to `[2.2.0,3.0.0)`; verify in the headless Equinox that all cloud bundles still resolve against their `[2.0.1,3.0.0)` imports
  - Checked in the headless Equinox: `org.elbe.relations` 2.4.0 and all three cloud bundles resolve. Also added, for the log requirement: `IPeerTransferListener.transferEnded(peerId, outcome, cause)` as a default method.
- [x] 3.3 Pass the prepared scope into `RelationsExportFiles` so the session can compare it (2.5); verify with a provider-level test that `forRequest` is never called for a mismatched mode
  - Checked with a counting `ExportFiles` behind the real `ExportSender`: a mismatched request calls `forRequest` 0 times.

## 4. Desktop UI

- [ ] 4.1 Override the two-argument `approve` in `PeerApprovalPrompt`: show the six-digit code prominently and the peer id as secondary detail, and close the dialog through `display.asyncExec` when the future is cancelled, ignoring any later dialog result; verify in the GUI that the code matches the receiver's output and that the dialog disappears when the receiver disconnects before the user decides
  - Implemented; the GUI check is still open (the build environment has no display). The cancellation path is checked at provider level (2.1).
- [ ] 4.2 Add status and summary text for `VERSION_MISMATCH` and `SCOPE_MISMATCH` in `ExportToPeerAction.statusOf`, naming the prepared scope for the scope case; add the keys and the updated approval message to `RelationsMessages.properties` and `RelationsMessages_de.properties`; verify the key-parity check passes and both messages render in English and German
  - Implemented; the key-parity check passes (no key missing in either language). Rendering is not yet checked in the GUI.
- [ ] 4.3 Confirm the change log is cleared only on `COMPLETED`: run an incremental session against a receiver that requests `full`, and one whose `result` omits the file; verify with `DataService.getNumberOfEvents()` (or reopening the export dialog) that the change log count is unchanged after both
  - Code path: the change log is cleared only on `COMPLETED`, and scope mismatch or an incomplete `result` never yields it (checked at provider level). The database-level check is still open.

## 5. Protocol contract

- [x] 5.1 Rewrite `bundles/org.elbe.relations.peer.libp2p/PROTOCOL.md` for `/relations/sync/1.0.0`: transport, service tag `_relations-sync._udp.local.`, framing and 64 KiB limit, every frame type and field (`hello`, `request`, `manifest` with `sha256`, `result`, `error` with `code`, `offered`, `supported`), a sequence diagram, the version and scope rules, and the confirmation-code algorithm with one worked test vector; keep the mDNS peer-id caveats from the old document; verify each identifier in the document matches the constants in `ExportSender` and `Libp2pTransferHost`
  - Checked: protocol id, service tag, frame limit and error codes match `ExportSender` and `Libp2pTransferHost`; the test vector was computed by the sender's own `confirmationCode`.
- [x] 5.2 Update the `ExportSender` and `Libp2pPeerTransferProvider` Javadoc, and every comment citing `docs/peer-to-peer-protocol.md`, to name `/relations/sync/1.0.0` and `PROTOCOL.md`; verify `grep -rn "relations/export\|relations-export\|peer-to-peer-protocol" bundles/` returns nothing outside `bin/` and `target/`
  - Checked: the grep returns nothing.
- [x] 5.3 Rebuild the reference receiver from the 5.1 document alone, including the digest check and the confirmation-code computation; verify it completes a transfer against the real sender and reports a digest mismatch when fed deliberately corrupted bytes
  - Checked: the receiver (throwaway scaffolding in the session scratchpad, not in the repo) uses none of the sender's classes; it completes transfers and reports `digest-mismatch` for corrupted bytes.
- [ ] 5.4 Share `PROTOCOL.md` with the Android companion application's maintainer and record any disagreement on the confirmation code, `sha256` or `error.code` before the version is frozen; verify by a recorded answer (a note in this change's `design.md`)

## 6. End-to-end in the product

- [ ] 6.1 In the launched product (GUI), prepare a full export, connect the reference receiver via mDNS, compare the confirmation codes, approve, and receive; verify the received zip passes the receiver's digest check, the application's own import accepts it, and the status line reports completion
- [ ] 6.2 Repeat 6.1 with an incremental export; verify the change log is empty afterwards and that the cloud export dialog no longer offers the incremental choice
- [ ] 6.3 Run the failure paths (decline, disconnect while pending, version mismatch, scope mismatch, and an interruption mid-transfer); verify each produces a distinct status message and log entry and leaves the change log intact, which unblocks `add-export-to-peer` 11.5 for the in-band version case
- [ ] 6.4 Run 6.1 and 6.3 with Relations in German; verify every new or changed string is translated
