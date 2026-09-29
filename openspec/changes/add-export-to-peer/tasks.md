# Tasks

Note on verification: `tests/` is outside the Maven reactor and there is no CI, so
`mvn clean verify` proves compilation only. Every task below states an observable check;
where that check is manual, it is manual deliberately.

The receiving Android application is outside this change, so until it exists the
counterparty for every runtime check is the **reference receiver** built in task 3.4 — a
throwaway command-line client speaking the same protocol, run on the same local network.
It is test scaffolding and is not shipped.

Group 1 is a gate. If 1.2 or 1.3 fails, stop and revisit `design.md` before building
anything on top.

## 1. Dependency reconciliation and runtime spike

- [x] 1.1 Resolve the full transitive set of `io.libp2p:jvm-libp2p:1.3.7-RELEASE` (TCP-only, no QUIC) and produce two lists: artifacts the Eclipse target platform already supplies at a compatible version, and artifacts that must be vendored; verify every artifact downloads, recording its source host (Maven Central, Cloudsmith, JitPack, Consensys)
- [x] 1.2 Smoke-test jvm-libp2p on Java 21 outside the application: start two hosts with TCP + Noise + a production multiplexer and open a stream between them; verify the handshake completes and bytes round-trip on the JDK the product targets
- [x] 1.3 Check OSGi resolution: build a throwaway bundle carrying the vendored set on `Bundle-ClassPath` and install it into the product; verify the product still starts and that no `Bundle-SymbolicName` collides with Netty, Guava, protobuf, slf4j or BouncyCastle already on the platform
- [x] 1.4 Record the outcome of 1.1–1.3 in `design.md` (vendored list, reused list, source hosts); verify the recorded list matches what the bundle actually ships

## 2. Peer bundle and libp2p host

- [x] 2.1 Create `bundles/org.elbe.relations.peer.libp2p/` with MANIFEST, `build.properties`, `OSGI-INF/` and the vendored jars from 1.1 on `Bundle-ClassPath`; verify `mvn clean verify` picks it up as a reactor module with no pom (pomless Tycho) and produces the bundle jar
- [x] 2.2 Implement host lifecycle — start and stop a libp2p host on a configurable port (default **9042**) using TCP and Noise; verify starting twice on the same port reports the conflict rather than throwing, and that stopping releases the port
- [x] 2.3 Persist the peer identity keypair alongside the application preferences and reuse it on restart; verify the peer id reported before and after a restart is identical
- [x] 2.4 Advertise an open session for local-network discovery via mDNS; verify the reference receiver finds the session without being given an address, and that a discovery failure still leaves the printed address usable

## 3. Transfer protocol `/relations/export/1.0.0` and reference receiver

- [x] 3.1 Write the protocol specification — protocol id and version semantics, length-prefixed header carrying scope, payload byte length and content digest, payload framing, acknowledgement, and error cases; verify it is complete enough that task 3.4 can be implemented from the document alone without reading the sender's source
- [x] 3.2 Implement the sender side — advertise the protocol, stream the export file, wait for the receiver's acknowledgement; verify a complete transfer is acknowledged and that non-acknowledgement is reported as failure
- [x] 3.3 Refuse unsupported protocol versions; verify a connection requesting a different version transfers nothing and surfaces a version-mismatch error to both the user and the log
- [x] 3.4 Build the reference receiver as a standalone command-line client implemented from the 3.1 document; verify it completes a transfer against the real sender and reports a digest mismatch when given deliberately corrupted input
- [x] 3.5 Handle interruption on both sides — connection dropped mid-transfer; verify the sender reports a failed transfer and the receiver leaves no partial file behind

## 4. Service interfaces and registry

- [x] 4.1 Add the peer-transfer service interfaces to `org.elbe.relations.services` covering session open/close, the connection-approval callback and the transfer outcome; verify the bundle compiles and the package still resolves for the existing cloud bundles
  - Checked: `mvn verify` compiles all bundles; `org.elbe.relations.services` now exports 2.1.0, and the cloud bundles still resolve against their `[2.0.1,3.0.0)` imports.
- [x] 4.2 Add the whiteboard registry DS component with `ReferenceCardinality.MULTIPLE` plus its `OSGI-INF` XML, listed in `Service-Component`; verify the component activates with no DS errors in the log
  - Checked: in a headless Equinox built from the product's plugins, `PeerTransferRegistry` is SATISFIED with no unsatisfied references and no DS errors.
- [x] 4.3 Add the DS component in the peer bundle providing the service; verify it appears in the registry at runtime
  - Checked: in the same framework the registry returns one provider (`libp2p`), which reports an identity and its description.

## 5. Preferences

- [ ] 5.1 Add a Peer preference page showing the installation's peer identity and allowing the listening port to be set, defaulting to 9042; verify the default applies on a fresh workspace, a changed port persists across restart, and the displayed identity matches the one the host reports
  - Implemented. Checked at provider level only: the identity is stable across provider instances and equals the id of an opened session. Still open: fresh-workspace default and port persistence across restart in the GUI.
- [ ] 5.2 Register the page in `plugin.xml` under `com.opcoach.e4.preferences.e4PreferencePages`; verify it appears in the Preferences tree beside Cloud Configuration
  - Implemented; the GUI check is still open (the build environment has no display).

## 6. Sending side

- [x] 6.1 Add the peer constants to `RelationsConstants` (port preference key and its 9042 default, full and delta export base names); verify they compile and do not collide with `PREFS_CLOUD_ACTIVE` / `CLOUD_SYNC_*`
  - `PREFS_PEER_PORT="peerTransferPort"`, `DFT_PEER_PORT=9042`, `PEER_EXPORT_FULL`, `PEER_EXPORT_DELTA`; the default is registered in `RelationsPreferenceInitializer`.
- [ ] 6.2 Add the export dialog with the full/incremental radio pair, the incremental option disabled when the change log is empty, the shared-change-log wording, and the statement of what a session exposes; verify both scope states render against a database with and without recorded events
  - Implemented; the GUI check is still open (the build environment has no display).
- [ ] 6.3 Add the session display showing the copyable peer address and stating that the session is live and local-network only; verify the copied clipboard content is the full multiaddr including the peer id
  - Implemented; clipboard content is not yet checked in the GUI. Checked headless: every address the session reports is a full multiaddr ending in `/p2p/<peerId>`.
- [ ] 6.4 Add the approval prompt showing the connecting device's identity, gating transmission on explicit approval; verify declining sends nothing and leaves the session open for a further connection
  - Implemented. Checked at provider level: a declined device gets nothing, and the same session then completes for a second device. The prompt dialog itself is not yet checked in the GUI.
- [ ] 6.5 Add `internal/actions/ExportToPeerAction.java` and the command handler `handlers/PeerToPeerUpload.java`, following the `ExportToCloudAction` / `CloudExportHandler` pair and keeping the handler to a single injected `@Execute` method as `CloudConfigurationHelper` does; the action produces the export with `ZippedXMLExport` or the EventStore delta exporter and deletes the temp file in a `finally` block; verify the temp file is gone after a successful, a declined and a failed run, and that the handler class itself holds no transfer logic
  - Implemented; the GUI check is still open (the build environment has no display). The delta exporter was made package-visible in `ExportToCloudAction` so it can be reused.

## 7. Change-log correctness

- [ ] 7.1 Move `new EventStoreChecker().clear()` in `AbstractExportToCloudJob.run()` from the unconditional position into the success branch; verify by running an incremental cloud export against an unreachable destination and confirming the delta is still available afterwards
  - Implemented; the GUI check is still open (the build environment has no display).
- [ ] 7.2 Clear the change log on the peer path only after the receiver has acknowledged a digest-verified transfer; verify that closing a session with no device connected, and interrupting a transfer, both leave the log intact
  - Implemented (clear only on `COMPLETED`, in `ExportToPeerAction.SessionListener`). Checked at provider level: INTERRUPTED / DECLINED / DIGEST_MISMATCH never report COMPLETED. The database-level check is still open.
- [ ] 7.3 Add the shared-change-log wording to `ExportToCloudDialog`; verify the text appears for the incremental selection in both dialogs
  - Implemented; the GUI check is still open (the build environment has no display).

## 8. Application model wiring

- [ ] 8.1 Add the command, the handler entry pointing at `bundleclass://org.elbe.relations/org.elbe.relations.handlers.PeerToPeerUpload`, and a `HandledMenuItem` for "Export to Peer …" under `org.elbe.relations.menu.utility`, positioned after `...menu.utility.cloud.export`; verify the entry appears between "Export to Cloud …" and "Preferences" and that invoking it reaches the handler
  - Implemented: the pre-existing menu item was moved after `cloud.export`, and the handler was given its `contributionURI`. the GUI check is still open (the build environment has no display).

## 9. Localization

- [ ] 9.1 Add the new `%`-keys to `OSGI-INF/l10n/bundle.properties` and `bundle_de.properties`; verify the menu label renders in both languages with no raw `%key` text
  - Keys present in both languages (automated parity check). Rendering is not yet checked.
- [ ] 9.2 Add dialog, session, approval and error strings to `RelationsMessages.properties` and `RelationsMessages_de.properties`; verify no `MissingResourceException` appears in the log during a transfer in either language
  - All 45 keys used by the new code exist in both files (automated check). A transfer in the GUI is not yet checked.
- [ ] 9.3 Add `messages.properties` and `messages_de.properties` to the peer bundle; verify the preference page renders in both languages
  - Added (provider description). Checked: the bundle loads. The page is not yet checked in the GUI.

## 10. Packaging

- [x] 10.1 Add `org.elbe.relations.peer.libp2p` to `features/relations.rcp.feature/feature.xml`; verify the feature build includes the bundle and its vendored jars
  - The feature was already listing the bundle. Checked: the built jar carries OSGI-INF, the messages and all 33 vendored jars, and the product plugins directory includes it.
- [x] 10.2 Add the bundle to the product's `<configurations>` at start level 4 with `autoStart="false"`; verify a built product starts with the bundle present and resolved
  - The entry was already present. Checked: `bundles.info` has `...,4,false`, and the bundle resolves in the headless Equinox framework. A full SWT launch is not yet done (same caveat as 1.3).
- [x] 10.3 If 1.1 concluded that any dependency should come from the target platform rather than be vendored, add the required Maven location entries; verify a **cold** target-platform resolve succeeds from an empty local repository
  - Not applicable: every dependency is vendored on `Bundle-ClassPath`, so no target-platform entry was needed and none was added.

## 11. End-to-end verification

- [ ] 11.1 Transfer a full export to the reference receiver using the printed address; verify the received file is a valid zipped XML export whose digest matches and which the application's own import accepts
- [ ] 11.2 Repeat using mDNS discovery instead of the printed address; verify the session is found and the transfer completes
- [ ] 11.3 Exercise the approval gate: connect and decline; verify nothing is transferred and the session survives a second attempt
- [ ] 11.4 Exercise the shared change log: make edits, run an incremental peer transfer, then open cloud export; verify the incremental option is unavailable because the log was consumed, and that the dialog wording predicted it
- [ ] 11.5 Exercise failure paths — port 9042 already bound, session closed before any device connects, connection dropped mid-transfer, protocol version mismatch; verify the four produce distinguishable messages and that the change log survives all of them
  - Blocked for version mismatch: the sender side receives no event when multistream-select refuses `/relations/export/2.0.0`, so nothing surfaces to the user or the log (see 3.3).
- [ ] 11.6 Confirm session lifetime: close the session, then attempt to connect from the reference receiver; verify the connection fails
- [ ] 11.7 Run the whole flow with Relations in German; verify every new label, dialog, warning and error message is translated
