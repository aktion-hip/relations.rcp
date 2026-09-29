# Design

## Context

See `proposal.md` — Why. The relevant current state:

The cloud export path is a fixed pipeline shared by all three providers:
`CloudExportHandler` → `ExportToCloudAction` (reads the active provider from preferences,
opens `ExportToCloudDialog`) → an `AbstractExportToCloudJob` subclass that (1) writes a
zipped XML export to a temp file, (2) hands the file to `ICloudProvider.upload(...)`, and
(3) clears the EventStore.

Two properties of that pipeline shape this design:

- It is fire-and-forget. `upload(...)` returns `boolean` and the user sees a status-line
  message. A peer transfer is instead a *session* with a lifetime, a counterparty, and an
  approval step, so it cannot reuse that shape.
- `AbstractExportToCloudJob.run()` calls `new EventStoreChecker().clear()` **outside** the
  success branch, so the change log is emptied even when the upload fails. With two
  destinations sharing the log this becomes a way to lose data silently.

## Goals / Non-Goals

**Goals:**

- Transfer the existing zipped XML export from Relations to the user's own mobile
  companion application over an encrypted, peer-authenticated connection on the local
  network, with no server and no storage account.
- Reuse the existing export producers (`ZippedXMLExport`, the EventStore delta exporter)
  unchanged.
- Specify the wire protocol well enough that the Android side can be implemented against
  it independently.
- Keep the peer capability optional and independently installable.

**Non-Goals:**

- **The receiving application.** The counterparty is a separate Android product, outside
  this repository and this change. This change delivers the sending half and the protocol
  contract only.
- **Traversing NAT.** Both devices are on the same local network — a desktop and the
  user's phone on the same WiFi. Connecting hosts across the internet is out of scope; see
  Decisions.
- **Content addressing or durable retrieval.** There is no CID and no address that
  outlives the session. An export is obtainable only while Relations holds the session
  open.
- Interoperating with IPFS, or with any client other than the companion application.
- Running a relay or any other always-on infrastructure.
- Any change to the domain model, schema, or `dbCreateObjects.xml`.

## Decisions

### Transport: embedded `io.libp2p:jvm-libp2p`, at the cost of reach

jvm-libp2p was chosen at the user's direction over the previously designed external
IPFS/Kubo daemon. It is a healthy project — 1.3.7-RELEASE on 2026-09-14, eight releases
in 2026, Apache-2.0 — but it is a *connection* layer, and the boundary matters:

| Provided | Absent (verified: zero files in the source tree) |
|---|---|
| TCP (production), QUIC/WebSocket (beta) | `bitswap` — no block exchange |
| Noise (production), TLS, multistream-select | `unixfs`, `cid` — no content addressing |
| mplex (production), yamux (beta) | `dht`, `kad` — no content or peer routing |
| gossipsub, identify, ping | `dcutr`, `holepunch`, `upnp` — no NAT traversal |
| mDNS discovery (beta), AutoNAT (beta) | record store, rendezvous, bootstrap |

What it gives is an authenticated, encrypted, multiplexed byte stream to a peer *whose
identity and address are already known*. Everything above that is ours.

The trade against the rejected alternative is stated plainly because it is not
favourable on every axis:

| | Kubo over HTTP RPC | jvm-libp2p (chosen) |
|---|---|---|
| Vendored jars | 0 | ~28 (TCP-only) |
| NAT traversal | works (DHT + relays) | none |
| Identifier | durable CID | session-scoped multiaddr |
| Survives app exit | yes | no |
| External prerequisite | a Kubo daemon | none |

The one axis jvm-libp2p wins — no external daemon — is the reason for the choice. The
rest is accepted cost and is reflected in the spec, which no longer promises a durable
identifier.

Also considered and not chosen: layering **Peergos/Nabu** (Bitswap, UnixFS, CID, DHT over
jvm-libp2p) to recover content addressing. It would preserve the original spec but is
also off Maven Central and multiplies an already large hand-vendored tree, while still
inheriting jvm-libp2p's NAT gaps.

### A custom wire protocol, because there is no block exchange

With no Bitswap and no UnixFS, the transfer is a purpose-built libp2p protocol —
`/relations/export/1.0.0`. The sender advertises it; the receiver opens a stream; the
sender writes a small header (scope, byte length, digest) followed by the zip bytes; the
receiver verifies the digest and acknowledges. The acknowledgement is what marks the
transfer complete, which the change-log rule depends on.

This is roughly two hundred lines. It is smaller and more auditable than adapting Bitswap
would be, but it is protocol code we own, including its framing and failure handling.

### Discovery: mDNS on the local network, plus a pasted address

Peer discovery is mDNS (marked beta upstream) for the same-LAN case, and manual entry of
the sender's multiaddr otherwise. There is no DHT, no rendezvous and no bootstrap list, so
there is no mechanism by which two peers find each other across the internet. This is why
NAT traversal is a non-goal rather than a risk to mitigate: without both routing *and*
hole punching, an internet-wide transfer has no path.

Circuit relay v2 exists upstream but is beta with open correctness defects — PR #503
(open, 2026-07-13) reports that the relay client never advertised its relayed addresses
and could not renew reservations; issue #339 (open since 2023-10-16) reports
`NetworkImpl` throwing on any `/p2p-circuit/` dial. Depending on it would be depending on
known-broken code, and it would additionally require operating a relay.

### The counterparty is an external Android application

The receiver is a companion app on the user's own phone, not another Relations instance.
Three consequences follow, and they are the main reason this design differs from a
publish-to-a-network design:

**The protocol is an interop contract, not an internal detail.** It is implemented twice,
by two teams, on two platforms, and the two halves ship independently. It therefore
carries an explicit version in its protocol id (`/relations/export/1.0.0`), and a
mismatch is refused rather than negotiated. The framing must be documented well enough to
implement from the document alone.

**Library choices on this side constrain the other side.** The transfer uses **TCP with
Noise** — both marked production upstream — and avoids QUIC. This is partly because QUIC
is beta in jvm-libp2p, and partly because its Netty dependency drags in native transport
artifacts with per-OS classifiers that are awkward on Android. Keeping to TCP and a
standard multiplexer leaves the Android implementation free to use whichever libp2p
binding suits it, or to implement the handshake directly.

**Nothing here is end-to-end verifiable on its own.** Until the Android app exists there is
no counterparty, so the sending half cannot be exercised. A minimal reference receiver is
therefore needed as a **verification harness** — a throwaway command-line client speaking
the same protocol, used to check the session, approval, transfer and failure paths. It is
test scaffolding, not a shipped artifact, and it doubles as the executable check that the
protocol document is complete enough to implement from.

### Discovery announces addresses, not identity

jvm-libp2p's mDNS corrupts identity-multihash peer ids — the form Ed25519 keys produce and
the form this feature uses. It prepends one character, consistently and only for that id
form; RSA-style `Qm…` ids survive intact. The announced *addresses* are correct.

Discovery is therefore used for addresses only. The announced id is passed to `connect()`
purely because libp2p requires a `/p2p/` component to dial, and the receiver retries with
the stray character removed if the announced form fails. Authentication is unaffected: the
Noise handshake establishes the real identity, so a wrong hint cannot connect to the wrong
device, only fail. The announced id is never displayed, pinned, or used for approval.

Two further traps found while implementing this, both recorded in `PROTOCOL.md` because the
Android side hits them too:

- The mDNS service tag **must** end in `.local.`. Without it the name parser throws
  `StringIndexOutOfBoundsException` — including for libp2p's own `MDnsDiscovery.ServiceTag`
  constant, which cannot be used with its own constructor.
- `MDnsDiscovery.expandWildcardAddresses()` is not usable for building the address shown to
  the user: it expands the IPv6 wildcard but returns an IPv4 wildcard unchanged, drops the
  `/p2p/` component, and emits IPv6 zone suffixes that are invalid in a multiaddr. The host
  enumerates `NetworkInterface` addresses directly instead, which behaves the same under
  either IP stack.

### Same-network scope is a fit, not a compromise

With a desktop and the user's own phone on one WiFi, mDNS discovery covers the intended
case directly and the absence of DHT, relays and hole punching costs nothing. The earlier
concern about jvm-libp2p's missing NAT traversal applied to connecting two strangers
across the internet; that is not this feature. The limitation is recorded honestly in the
spec — the session is local-network only — rather than treated as a gap to close later.

### Dependency handling: measured, not estimated (task 1.1 result)

The transitive compile+runtime set of `io.libp2p:jvm-libp2p:1.3.7-RELEASE`, excluding QUIC
and tcnative for the TCP-only configuration, is **33 artifacts, ~23 MB**. All 33 were
downloaded successfully. Source hosts: **30 Maven Central**, 1 Cloudsmith (`jvm-libp2p`),
1 JitPack (`java-multibase`), 1 Consensys (`noise-java`).

The earlier "~28, or ~38 with QUIC" estimate came from counting per-OS classifier
artifacts; dropping QUIC and tcnative removes 11 of them.

**Reusable from the target platform** (versions read from the p2 repositories the target
file pins):

| Artifact | libp2p wants | Orbit 2024-12 | Eclipse 2026-03 |
|---|---|---|---|
| `com.google.guava` | 33.3.1-jre | **33.3.1.jre** (exact) | 33.5.0.jre |
| `com.google.guava.failureaccess` | 1.0.2 | **1.0.2** (exact) | 1.0.3 |
| `slf4j.api` | 2.0.9 | 2.0.16 | 2.0.17 |
| `bcprov` / `bcpkix` | 1.78.1 | 1.79.0 | 1.83.0 |

**Must be vendored** — everything else, notably:

- All **11 Netty modules**. Netty is not present in either p2 repository at all.
- `protobuf-java` 3.25.5 (see the collision below).
- The six artifacts with **no OSGi metadata whatsoever**: `jvm-libp2p`, `kotlin-stdlib`,
  `kotlin-stdlib-jdk8`, `kotlinx-coroutines-core-jvm`, `java-multibase`, `noise-java`.
- The annotation-only jars `checker-qual`, `jsr305`, `error_prone_annotations`,
  `j2objc-annotations`, `listenablefuture`, none of which are on the platform.

**The protobuf symbolic name is contested three ways.** `protobuf-java` 3.25.5 and
`protobuf-javanano` 3.0.0-alpha-5 *both* declare `Bundle-SymbolicName: com.google.protobuf`,
and Eclipse 2026-03 ships a third bundle with that same name at version
**2.4.0.v201105131100** — protobuf 2.4, from 2011. Only one bundle with a given symbolic
name can resolve.

This is precisely why the provider bundle keeps its dependencies on `Bundle-ClassPath`
rather than importing their packages: the bundle's own classloader resolves its vendored
protobuf first, and the platform's 2011 copy is never consulted. `protobuf-javanano`
arrives only through `netty-codec-protobuf`'s nano support, is not used here, and should
be excluded outright rather than shipped alongside its namesake.

Kotlin stdlib is pinned at **1.6.21** (2022) by jvm-libp2p and becomes part of the
product's runtime.

Caveat on the reuse column: those versions were read directly from the p2 repositories'
content indexes. Whether Tycho actually resolves them into the build is what task 1.3
confirms.

### The command handler stays thin

The handler is `org.elbe.relations.handlers.PeerToPeerUpload`, matching the convention of
its neighbours in that package: a single `@Execute` method whose parameters are supplied by
injection, which opens a dialog and returns. `CloudConfigurationHelper` is the closest
existing example — it takes the active shell, a DS registry and the logger, opens its
dialog, and holds no logic of its own; `CloudExportHandler` is thinner still, delegating
immediately to `ExportToCloudAction`.

Session lifecycle, approval and transfer therefore live in the action and the peer bundle,
not the handler. This keeps the e4-facing class free of anything that would need a running
workbench to test, and it is why the handler is named for what the user invokes
(`PeerToPeerUpload`) rather than for the machinery behind it.

### Peer identity is persisted

The libp2p keypair is generated once and stored with the application's preferences, so a
shared address keeps identifying the same installation across restarts, as the spec
requires. The identity is the installation's, not the user's, and is not tied to any
account.

### An explicit approval gate

libp2p authenticates the remote peer but says nothing about authorization: any peer that
reaches the listener could open the protocol stream. Because the payload is the user's
entire knowledge base, the sender is shown the connecting peer's identity and must
approve before any bytes flow. This is cheap to implement and is the main control
protecting the session.

### Change log cleared on acknowledged delivery

The clear moves into the path taken only when the receiver has acknowledged a complete,
digest-verified transfer. This also **moves the existing unconditional `clear()` in
`AbstractExportToCloudJob` into its success branch** — a behavioral fix to the cloud path,
in scope because the shared-log design depends on it and because otherwise a failed peer
transfer would destroy the cloud delta.

## Gate results (group 1, executed)

**1.2 — jvm-libp2p runs on Java 21.** Two hosts built with `HostBuilder` (TCP + Noise +
mplex) on Temurin 21.0.12.1: Noise handshake completed, the dialled peer id matched the
authenticated remote identity, three ping round-trips succeeded (5/2/2 ms), and the
listening port was released after `stop()`. The Java-facing DSL is sufficient — no Kotlin
is needed in our own code. Listen addresses already carry the `/p2p/<peerId>` suffix the
session display needs.

**1.3 — the vendored stack resolves and runs inside OSGi.** A spike bundle carrying all 33
jars on `Bundle-ClassPath` (21 MB) reached `ACTIVE` in the product's own Equinox
3.24.100, started a libp2p host under `EquinoxClassLoader`, and stopped cleanly.

Two concrete requirements came out of it:

- **`Import-Package` must list the `javax.*` packages.** OSGi auto-delegates only `java.*`.
  Without `javax.crypto`, `javax.crypto.spec`, `javax.net.ssl` and friends, BouncyCastle
  fails during `BouncyCastleProvider.<clinit>` with `NoClassDefFoundError:
  javax/crypto/spec/DHParameterSpec` — at bundle activation, before any transfer. The
  working set is: `javax.crypto[.interfaces|.spec]`, `javax.net[.ssl]`,
  `javax.security.auth.x500`, `javax.security.cert`, `javax.management`,
  `javax.naming[.directory|.ldap]`, `javax.xml.parsers`, `org.w3c.dom`, `org.xml.sax`.
- **Netty needs boot delegation for `sun.misc`** (`sun.*,com.sun.*,jdk.internal.*`), as it
  reaches for `Unsafe`.

**Symbolic-name collisions are a non-issue.** Measured against all 260 BSNs in the built
product: Netty (all 11 modules), `com.google.protobuf`, `com.google.guava` and
`com.google.guava.failureaccess` are **not in the product at all**. Only `slf4j.api`
(2.0.16), `bcprov` (1.79.0) and `org.jsr-305` (3.0.2) overlap, and because dependencies
sit on `Bundle-ClassPath` rather than being imported as bundles, the spike ran ACTIVE
alongside all three. The earlier concern about the platform's 2011-era
`com.google.protobuf` 2.4.0 does not arise: that bundle exists in the p2 repository but is
not included in the product.

**2.2 — a wildcard listen address is not a shareable address.** Binding
`/ip4/0.0.0.0/tcp/9042` makes the host report its address as
`/ip6/::/tcp/9042/p2p/<peerId>` — the wildcard, which is useless to a user trying to point
a phone at it. The session display (task 6.3) must therefore enumerate the machine's
actual non-loopback interface addresses and present those, not echo what
`listenAddresses()` returns. The spec's requirement that the address be presented "in a
form the user can copy" is only met by a concrete address.

**Not verified here:** a full GUI launch of the product. The build environment is headless,
so "the product still starts" was established at framework level (the product's own
Equinox, with the bundle ACTIVE) rather than by launching the SWT application. A GUI
smoke-launch remains worth doing once on a desktop.

**Build note:** `mvn clean verify` compiles and packages all 18 bundles and both features,
and materializes the product for linux/win32/macosx. On a Windows-mounted working copy the
final `archive-products` step fails while packing `klist.exe` inside the bundled JustJ JRE;
this is a filesystem artifact, not a build defect, and does not affect bundle output.

## Risks / Trade-offs

- **The feature is inert until the Android application exists** → This change delivers one
  half of a two-sided feature. Until the companion app ships, Export to Peer can open a
  session that nothing will connect to. The verification harness keeps the sending half
  provably correct in the meantime, but the user-visible feature should not be announced
  before its counterpart exists.
- **The two halves ship independently and can drift** → The protocol id carries a version
  and a mismatch is refused outright rather than half-transferring. Any framing change is
  a new version, never a silent alteration of `1.0.0`.
- **Transfers are local-network only** → Accepted and specified, not mitigated. This
  matches the intended use (desktop and the user's own phone on one WiFi), so the UI
  describes the feature in those terms rather than presenting a general P2P capability
  that happens to fail off-LAN.
- **~28 hand-vendored jars, three of them off Maven Central** → Reconcile against the
  target platform first and vendor only the remainder; record every vendored artifact and
  its source host in the bundle so the set is auditable later. This remains the single
  largest ongoing maintenance cost of the change.
- **OSGi symbolic-name collisions with the target platform** → Resolve before writing
  code (task 2.1). If a collision cannot be avoided, the provider bundle keeps its copy on
  `Bundle-ClassPath` rather than importing the package, at the cost of duplicate classes.
- **Kotlin 1.6.21 enters the product runtime** → No mitigation beyond awareness; it is a
  2022 stdlib that receives no updates and is pinned by the library.
- **mDNS and QUIC are beta upstream** → Use TCP, which is marked production. Treat mDNS
  discovery as a convenience and always support manual address entry, so a discovery
  failure never blocks a transfer.
- **We own a wire protocol** → Keep it minimal, length-prefixed and digest-verified, and
  treat any framing error as a failed transfer that leaves the change log intact.
- **The session model can surprise** → A user may close Relations expecting the export to
  remain available. The session display states the lifetime explicitly, and the spec
  requires it.
- **Java 21 is very likely but unverified** → jvm-libp2p targets Java 11 and is used in
  production by Teku; no JDK 17/21 defects are reported. Confirm with a smoke test on
  Java 21 before the rest of the work (task 2.2).
- **No automated verification** → `tests/` is outside the reactor and there is no CI, so
  this change cannot be validated by the build. Verification is manual and stated as such:
  two instances on one LAN, a failure run, and a German-language pass.

## Migration Plan

Additive. No schema change, no data migration, no change to existing exports on disk.

Deployment is the normal bundle route: new bundle into `relations.rcp.feature`, new entry
in the product's `<configurations>`, new preference page registration in `plugin.xml`.
Unlike the previous design this one also changes the dependency surface of the product,
so the target platform may gain Maven-location entries for whatever is reused rather than
vendored.

Rollback is removal of the bundle from the feature and product; no user data is altered.
The one edit that does not roll back with the bundle is the `clear()` move in
`AbstractExportToCloudJob` — an independent bug fix that should stay.

## Open Questions

Both previous open questions are now resolved: the listening port defaults to **9042**,
and there is no receive path in Relations — the counterparty is the Android application.

- Whether the mDNS service name should identify the application only, or also the
  installation. Identifying the installation makes a phone paired with several desktops
  easier to use; identifying only the application reveals less on a shared network. Either
  satisfies the spec, which requires only that a session on the same network be
  discoverable.
