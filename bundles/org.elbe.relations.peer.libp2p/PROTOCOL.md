# Relations Peer Sync Protocol — `/relations/sync/1.0.0`

This document specifies the wire protocol Relations (desktop) uses to hand an exported
knowledge base to a companion application. It is the single authoritative description of
the protocol: the receiving side is implemented from this document alone, without reading
the sender's source, and the sender is checked against it.

## 1. Roles and transport

| | |
|---|---|
| **Sender** | Relations desktop. Listens. Holds the prepared export. |
| **Receiver** | The companion application. Dials. Takes delivery. |

The connection is a libp2p stream:

- **Transport:** TCP. QUIC is not used.
- **Security:** Noise (`/noise`). The sender's peer identity is stable across restarts, so
  a receiver may pin it after first use.
- **Multiplexer:** mplex (`/mplex/6.7.0`).
- **Protocol id:** `/relations/sync/1.0.0`, negotiated by multistream-select.

The sender listens on a configurable TCP port, **9042** by default, and advertises itself
on the local network via mDNS while a session is open. Both devices must be on the same
local network; there is no relay, no hole punching and no DHT.

## 2. Discovery on the local network

While a session is open the sender advertises itself over mDNS under the service tag:

```
_relations-sync._udp.local.
```

The trailing `.local.` is part of the tag, not decoration. Browse for this exact string.

**Take the address from discovery. Do not trust the advertised peer identity.**

jvm-libp2p's mDNS implementation corrupts identity-multihash peer ids — the form Ed25519
keys produce, and the form Relations uses. Observed consistently:

```
  announced by mDNS : 412D3KooWAv1A9pyJsVn43oo47WRhq2eqpfyVaaZkiMSc6kPecHeL
  actual identity   :  12D3KooWAv1A9pyJsVn43oo47WRhq2eqpfyVaaZkiMSc6kPecHeL
                      ^ one spurious leading character
```

The *addresses* in the announcement are correct; only the identity is mangled. A receiver
should therefore:

1. Take an address from the announcement — these have no `/p2p/` component.
2. Treat the announced id as an untrusted hint. libp2p requires a `/p2p/` component to
   dial, so try the announced id, and on failure retry with its first character removed.
3. **Take the real identity from the Noise handshake** (`secureSession().getRemoteId()` or
   your binding's equivalent) once connected, and use that everywhere afterwards,
   including for the confirmation code (section 6).

This is safe regardless of the hint: the handshake authenticates the peer
cryptographically, so a wrong hint cannot connect you to the wrong device — it can only
fail to connect. Never display or pin the announced id.

Discovery is a convenience. When it is unavailable, the user can read the session address
from Relations and enter it directly; that address *does* carry a correct `/p2p/` component.

## 3. Framing

Control messages are **frames**: a 4-byte unsigned big-endian length, followed by that many
bytes of UTF-8 JSON encoding one JSON object.

```
+----------------+---------------------------+
| length (u32 BE)| JSON object, UTF-8        |
+----------------+---------------------------+
```

- A frame is at most **65 536** bytes of JSON. A larger length is a protocol error.
- Every object has a string field `type`. Unknown *fields* are ignored by both sides, so
  fields may be added without a new version. Unknown *types* are a protocol error.
- File contents are **not** framed: after the `manifest` frame the sender writes the raw
  bytes of each announced file, back to back, in manifest order. The receiver knows where
  each file ends from its `size`.

## 4. Frames

### `hello` — sender → receiver

Sent by the sender as soon as the stream opens, without waiting for anything.

```json
{"type":"hello","protocol":1,"name":"MY-DESKTOP"}
```

| Field | Type | Meaning |
|---|---|---|
| `protocol` | number | The protocol version, `1` for `/relations/sync/1.0.0` |
| `name` | string | The computer's name, for display in the receiver's confirmation dialog |

### `request` — receiver → sender

Sent once, after the receiver's user has accepted the connection on their side.

```json
{"type":"request","protocol":1,"mode":"incremental"}
```

| Field | Type | Meaning |
|---|---|---|
| `protocol` | number | The version the receiver speaks; must be `1` |
| `mode` | string | `"full"` or `"incremental"` |

### `manifest` — sender → receiver

Sent once both users have approved and the request matched (section 5). Lists the files
that follow.

```json
{"type":"manifest","files":[
  {"name":"relations_delta_20260930_1612.zip","size":48213,
   "sha256":"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"}]}
```

| Field | Type | Meaning |
|---|---|---|
| `files[].name` | string | File name. A full export is exactly one file, `relations_all.zip`. An incremental export is zero or more files named `relations_delta_…`, oldest first. |
| `files[].size` | number | Exact length in bytes of the raw content that follows |
| `files[].sha256` | string | SHA-256 of the file's content, 64 lowercase hex characters |

The files are zipped XML exports, byte-for-byte the format Relations uploads to cloud
storage. The Relations desktop currently sends exactly one file per transfer.

### `result` — receiver → sender

Sent once the receiver has received, verified and imported the files.

```json
{"type":"result","imported":["relations_delta_20260930_1612.zip"]}
```

| Field | Type | Meaning |
|---|---|---|
| `imported` | array of strings | Names of the files the receiver imported successfully |

The sender treats the transfer as **completed only if `imported` names every file in the
manifest**. Only then does it clear its change log. A `result` naming fewer files is a
failed transfer, and the change log is kept so the changes can be sent again. After a
`result` the sender closes the stream.

### `error` — either direction

Ends the exchange. The side that sends it closes the stream afterwards.

```json
{"type":"error","code":"scope-mismatch","message":"This computer offers an incremental export.","offered":"incremental"}
```

| Field | Type | Meaning |
|---|---|---|
| `code` | string | Machine-readable reason, from the table below |
| `message` | string | Human-readable English text, for logs; not for localized display |
| `offered` | string | Only with `scope-mismatch`: the mode the sender offers |
| `supported` | number | Only with `version-mismatch`: the version the sender supports |

| `code` | Sent by | Meaning |
|---|---|---|
| `declined` | sender | The desktop user declined this device |
| `busy` | sender | Another transfer is running; try again later |
| `version-mismatch` | sender | The `request.protocol` is not supported |
| `scope-mismatch` | sender | The `request.mode` differs from the export the user prepared |
| `no-export` | sender | The prepared export cannot be read |
| `protocol` | either | A frame was malformed or arrived out of order |
| `digest-mismatch` | receiver | A received file did not match its `sha256` |
| `storage` | receiver | The receiver could not store or import the files |
| any other | receiver | Treated by the sender as "the receiver rejected the transfer" |

A receiver may add its own codes; the sender treats an unknown code as a rejection, not as a
protocol error.

## 5. Sequence

```
Receiver (phone)                                Sender (Relations desktop)
      |  ---- TCP, Noise, mplex, /relations/sync/1.0.0 ---->  |
      |  <---------------- hello ---------------------------  |
      |                                                       |
      |  both users compare the 6-digit confirmation code     |
      |  (section 6) and accept, each on their own screen     |
      |                                                       |
      |  ---------------- request ------------------------->  |
      |                          (version check, scope check) |
      |               ... wait for the desktop user ...       |
      |  <---------------- manifest ------------------------  |
      |  <---------------- raw file bytes ------------------  |
      |  verify sha256, import                                |
      |  ---------------- result -------------------------->  |
      |                              sender closes the stream |
```

Rules:

1. The sender sends `hello` immediately. The receiver may send `request` at any time after
   it has read `hello`; the sender does not send `manifest` before its own user has
   approved, whichever comes first.
2. **Version.** The sender checks `request.protocol` before anything else. If it is not `1`
   it replies `error` with code `version-mismatch` and `"supported":1`, and sends nothing
   else.
3. **Scope.** The export the desktop user prepared is authoritative. If `request.mode`
   differs from it, the sender replies `error` with code `scope-mismatch` and `offered` set
   to the prepared mode, sends nothing else, and keeps its change log. The session stays
   open, so the receiver may reconnect and request the offered mode.
4. If the desktop user declines, the sender sends `error` with code `declined`. The session
   stays open for another connection.
5. Only one transfer runs at a time. A second stream opened while one runs receives `error`
   with code `busy`.
6. If the connection ends before the desktop user has decided, the approval prompt is
   withdrawn and nothing is sent.

Neither side retries automatically.

## 6. Confirmation code

Both devices display the same six-digit code so the users can check they are connecting
the devices they think they are. It is computed from the two peer ids **as established by
the Noise handshake** — the local id and the remote id of the secure session — never from
an mDNS announcement.

1. Take the raw bytes of both peer ids (the binary multihash, i.e. the base58-decoded form;
   for an Ed25519 key 38 bytes starting `00 24 08 01 12 20`).
2. Order the two byte strings by unsigned lexicographic comparison, smaller first.
3. Compute SHA-256 over the concatenation `smaller || larger`.
4. Read the first 4 bytes of the digest as an unsigned 32-bit big-endian integer.
5. Take it modulo 1 000 000 and format it as six decimal digits with leading zeros.

The result does not depend on which device computes it.

**Test vector.** Ed25519 private keys given as 32-byte seeds:

| | Seed (hex) | Peer id |
|---|---|---|
| A | `000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f` | `12D3KooWA4Xop1JaT3MHxwYMkCepYsv4iPVopMXwCz5iHYdBfeSB` |
| B | `fffefdfcfbfaf9f8f7f6f5f4f3f2f1f0efeeedecebeae9e8e7e6e5e4e3e2e1e0` | `12D3KooWNQHGF6MZJ5WBLJ1VSUcLHhynAiHURkPC1asyhoFgR5t9` |

```
bytes(A) = 00240801122003a107bff3ce10be1d70dd18e74bc09967e4d6309ba50d5f1ddc8664125531b8
bytes(B) = 002408011220bafc71bead3ac5e4b63e9c8216ee71a34aaec65722eedbca728b4e9b3ccce396
smaller  = A
SHA-256(A || B) = ecb47437b249c911ddc4d3e381758672bcbace50567ff014774b141447efec55
first 4 bytes   = 0xecb47437 = 3971249207
code            = 3971249207 mod 1000000 = 249207
```

## 7. Failure handling

| Situation | Sender behaviour |
|---|---|
| `request.protocol` ≠ `1` | `error` `version-mismatch`; nothing sent; reported as a version mismatch |
| `request.mode` ≠ prepared scope | `error` `scope-mismatch`; nothing sent; reported as a scope mismatch |
| Desktop user declines | `error` `declined`; session stays open |
| Stream closes before `result` | reported as an interrupted transfer; change log kept |
| `result` does not name every manifest file | reported as a failed transfer; change log kept |
| Receiver sends `error` | reported as failed, with the reason taken from `code`; change log kept |
| Frame malformed, too large, or out of order | `error` `protocol`; reported as a protocol error |
| User closes the session mid-transfer | the stream is closed; change log kept |

| Situation | Receiver behaviour |
|---|---|
| Fewer than `size` bytes arrive for a file | discard all partial files; send nothing |
| A file's SHA-256 differs from `sha256` | discard the files, send `error` `digest-mismatch` |
| Files cannot be stored or imported | send `error` `storage` |

A protocol id other than `/relations/sync/1.0.0` is refused by multistream-select before
any frame is exchanged, and the sender is not notified. Version negotiation therefore
happens in band, through the `protocol` fields, within this protocol id.

## 8. Versioning

The `1.0.0` framing — the length prefix and the frame types and their required fields —
never changes. Optional fields may be added to existing frames; receivers and senders
ignore fields they do not know. A change that breaks this is a new protocol id
(`/relations/sync/2.0.0`) and a new `protocol` number.

## 9. Security notes

- The Noise handshake authenticates both peers. The peer id the sender shows the user for
  approval is the identity that receives the bytes, and the confirmation code binds both
  identities so each user can check the other device.
- Approval is per connection and is not remembered. A receiver cannot assume a previous
  approval still holds.
- The payload is the user's complete knowledge base. A receiver should store it no more
  openly than the user's own data.
- The sender's identity is stable, so a receiver may pin it and warn if it changes.
