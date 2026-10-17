# Proposal

## Why

Relations can already publish a snapshot of the user's data, but only to a commercial
cloud account (Dropbox, Google Drive, MS Azure). That makes sharing dependent on a
third-party provider account, and the upload lands in private storage rather than
somewhere a second person can take delivery from.

An "Export to Peer …" action lets the user send the same export **directly from Relations
to a companion application on their own Android phone**, over an encrypted peer-to-peer
connection on the local network — no server, no storage account, no cable, and no copy
left on anyone else's infrastructure.

The receiving Android application is a separate product and is **not part of this change**.
What is delivered here is the sending half plus the protocol contract the Android side
implements against.

Two library decisions sit behind this, both recorded in `design.md`:

- **Hive2Hive**, named in the original request, was assessed and rejected: its core
  dependency (TomP2P) is no longer downloadable from any source, its serialization layer
  fails at construction on Java 17+, and its model is a login-based distributed filesystem
  rather than a way to hand over a file.
- **`io.libp2p:jvm-libp2p`** is used for the connection, chosen over an external
  IPFS/Kubo daemon. It is actively maintained, but it is a *connection* layer only — it
  has no content addressing, no block exchange, no DHT and no NAT traversal. That boundary
  is what shapes the scope below.

## What Changes

- Add an **"Export to Peer …"** entry to the Utility menu, directly below "Export to
  Cloud …", backed by a new command and handler.
- Opening the action starts a **live transfer session**: the application listens as a
  libp2p peer and displays a copyable address. The session exists only while it is open —
  closing it, or exiting Relations, ends availability. There is no durable identifier and
  nothing survives the session.
- The session is **discoverable on the local network** so the phone can find it without
  the user typing anything, with the address available as a fallback.
- Define and document a versioned transfer protocol, **`/relations/export/1.0.0`**, in
  enough detail for the Android application to be implemented against it independently. A
  version mismatch is refused rather than negotiated.
- Add a **per-connection approval gate**: the user sees the connecting device's
  cryptographic identity and must approve before any bytes flow. libp2p authenticates
  peers but does not authorize them, and the payload is the user's entire knowledge base.
- Build a throwaway **reference receiver** as a verification harness. Until the Android
  app exists there is no counterparty, so nothing in this change is otherwise testable
  end to end. It is test scaffolding, not shipped.
- Add a **peer-transfer SPI** and whiteboard registry alongside the existing cloud SPI,
  following the established DS pattern, and a new bundle
  **`org.elbe.relations.peer.libp2p`** implementing it.
- Add a **"Peer" preference page** showing the installation's persistent peer identity and
  allowing the listening port to be configured, defaulting to **9042**.
- Peer export offers the same **full / incremental** choice as cloud export and
  **deliberately shares the single EventStore chain** with it. Both export dialogs gain
  wording naming the shared log.
- **BREAKING (behavioral, existing code):** the change log is currently cleared even when
  an export fails. It will instead be cleared only after a completed, acknowledged
  transfer. This fixes silent data loss in the existing cloud path.
- Add English and German strings for every new user-facing label.

**Explicitly not delivered:** the Android application itself, and transfers across the
internet. jvm-libp2p ships no hole punching or port mapping and its circuit relay is beta
with open correctness defects, so transfers require both devices on the same local
network — which is the intended use, a desktop and the user's own phone on one WiFi.

## Capabilities

### New Capabilities

- `peer-export`: sending the user's exported data directly to a companion application on
  their own device over a peer-to-peer connection on the local network — menu action, peer
  identity and port configuration, full vs. incremental selection, the transfer session
  and its lifetime, local-network discovery, connection approval, the versioned transfer
  protocol, and how results and failures are reported.

### Modified Capabilities

None. The project has no existing specs (`openspec list --specs` is empty), so there is no
cloud-export capability to amend. The shared-change-log wording added to
`ExportToCloudDialog` and the corrected clear-on-success behavior are user-visible changes
to an unspecified capability; they are covered here because this change introduces the
conflict that requires them.

## Impact

**New bundle**
- `bundles/org.elbe.relations.peer.libp2p/` — DS components, the libp2p host, the
  `/relations/export/1.0.0` protocol, and the configuration UI.
- Carries the vendored dependency set. jvm-libp2p pulls roughly **28 transitive artifacts**
  for a TCP-only configuration. Several (Guava, slf4j, Netty, protobuf, BouncyCastle) may
  already exist in the target platform and must be reconciled rather than duplicated;
  jvm-libp2p itself, the Kotlin standard library, kotlinx-coroutines, `java-multibase` and
  `noise-java` carry no OSGi metadata and will be vendored. Three artifacts are **not on
  Maven Central** and come from Cloudsmith, JitPack and a Consensys repository.
- Added to `features/relations.rcp.feature/feature.xml` and to the product's
  `<configurations>` at start level 4, `autoStart="false"`, matching the cloud bundles.

**Possible target-platform change**
- Reusing platform copies of Netty/Guava/protobuf instead of vendoring may require Maven
  location entries. Target-platform edits are high risk in this project and must be
  verified with a cold resolve.

**Modified in `org.elbe.relations`**
- `Relations.e4xmi` — command, handler and `HandledMenuItem` for the export action.
- New: `handlers/PeerToPeerUpload.java`, the command handler that starts the peer export
  workflow. It follows the shape of the existing handlers in that package — a thin
  `@Execute` method taking its collaborators by injection and opening the dialog — with no
  transfer logic of its own.
- New service interfaces, registry, action, scope dialog, session display, approval dialog
  and preference page.
- Modified: `RelationsConstants.java`, `internal/utility/ExportToCloudDialog.java`,
  `internal/utility/AbstractExportToCloudJob.java` (the clear-on-success fix),
  `plugin.xml`, `OSGI-INF/l10n/bundle.properties` and `bundle_de.properties`,
  `RelationsMessages.properties` and its `_de` sibling.

**Unchanged**
- The data layer. No schema change, no `dbCreateObjects.xml` or XSL edit, and no upgrade
  path needed for existing user databases — a direct consequence of reusing the existing
  EventStore chain rather than adding per-destination watermarks.
- `org.hip.viffw`, the indexer, and all existing cloud provider bundles.

**Risk**
- A transfer session exposes the complete knowledge base to any device that can reach the
  listening address on the local network; the approval gate is the control, and it must be
  in place before the first byte is sent.
- The dependency set is the largest ongoing cost: ~28 hand-managed binaries from three
  suppliers, including a Kotlin 1.6.21 runtime pinned by the library, in a project with no
  dependency scanning and no CI.
- This is one half of a two-sided feature. Until the Android application ships, Export to
  Peer opens a session nothing will connect to, so the user-visible feature should not be
  announced before its counterpart exists.
