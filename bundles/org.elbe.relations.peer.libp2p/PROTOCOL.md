# Relations Peer Export Protocol — `/relations/export/1.0.0`

This document specifies the wire protocol Relations (desktop) uses to hand an exported
knowledge base to a companion application. It is written so the receiving side can be
implemented from this document alone, without reading the sender's source.

## 1. Roles and transport

| | |
|---|---|
| **Sender** | Relations desktop. Listens. Holds the export. |
| **Receiver** | The companion application. Dials. Takes delivery. |

The connection is a libp2p stream:

- **Transport:** TCP. QUIC is not used.
- **Security:** Noise (`/noise`). The sender's peer identity is stable across restarts, so
  a receiver may pin it after first use.
- **Multiplexer:** mplex (`/mplex/6.7.0`).
- **Protocol id:** `/relations/export/1.0.0`, negotiated by multistream-select.

The sender listens on a configurable TCP port, **9042** by default, and advertises itself
on the local network via mDNS while a session is open. Both devices must be on the same
local network; there is no relay, no hole punching and no DHT.

## 1a. Discovery on the local network

While a session is open the sender advertises itself over mDNS under the service tag:

```
_relations-export._udp.local.
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
   your binding's equivalent) once connected, and use that everywhere afterwards.

This is safe regardless of the hint: the handshake authenticates the peer
cryptographically, so a wrong hint cannot connect you to the wrong device — it can only
fail to connect. Never display or pin the announced id.

Discovery is a convenience. When it is unavailable, the user can read the session address
from Relations and enter it directly; that address *does* carry a correct `/p2p/` component.

## 2. Versioning

The version lives in the protocol id. A receiver requesting a protocol id the sender does
not support is refused by multistream-select before any application bytes are exchanged;
the sender reports this as a version mismatch and transfers nothing.

Any change to the framing below is a **new protocol id** (`/relations/export/1.1.0`,
`/relations/export/2.0.0`, …). The framing of `1.0.0` never changes. A sender may announce
several protocol ids; a receiver should request the newest it understands.

## 3. Session sequence

```
  Receiver                                  Sender (Relations)
     |                                              |
     |-- dial multiaddr, Noise handshake ---------->|
     |                                              |  user is shown the
     |                                              |  receiver's peer id
     |                                              |  and must approve
     |-- open stream /relations/export/1.0.0 ------>|
     |                                              |
     |<------------------------- HEADER ------------|   (§4)
     |<------------------------- PAYLOAD -----------|   (§5)
     |                                              |
     |-- ACK -------------------------------------->|   (§6)
     |                                              |
     |     both sides close the stream              |
```

The sender writes nothing until the user has approved the connection. A receiver may wait
an unbounded time at that point, or give up; abandoning the stream is not an error.

## 4. Header

Written by the sender, immediately after approval. All integers are **big-endian and
unsigned**. Fields appear in exactly this order with no padding.

| Offset | Size | Field | Value |
|---|---|---|---|
| 0 | 4 | `magic` | ASCII `RLEX` (`0x52 0x4C 0x45 0x58`) |
| 4 | 1 | `scope` | `0x01` full export, `0x02` incremental export |
| 5 | 8 | `payloadLength` | length of §5 in bytes |
| 13 | 1 | `digestAlgorithm` | `0x01` = SHA-256 |
| 14 | 1 | `digestLength` | `32` for SHA-256 |
| 15 | *digestLength* | `digest` | digest of the payload bytes |
| 15+*d* | 2 | `nameLength` | length of `name`, ≤ 255 |
| 17+*d* | *nameLength* | `name` | UTF-8 suggested file name, e.g. `relations_all.zip` |

A receiver MUST reject the stream if `magic` does not match, if `digestAlgorithm` is not
`0x01`, or if `digestLength` disagrees with the algorithm. It SHOULD apply its own upper
bound to `payloadLength` before allocating storage.

## 5. Payload

Exactly `payloadLength` bytes, written directly after the header with no additional
framing. The payload is the same **zipped XML export** that Relations produces for cloud
export, so an existing Relations import accepts it unchanged.

- `scope = 0x01` — the complete data set: `TermEntries`, `TextEntries`, `PersonEntries`,
  `RelationEntries`.
- `scope = 0x02` — only `EventStoreEntries`, the changes recorded since the previous
  export.

Nothing in this protocol interprets the payload. A receiver that only stores the file needs
no knowledge of its contents.

## 6. Acknowledgement

Written by the receiver once it has read `payloadLength` bytes and checked the digest.

| Offset | Size | Field | Value |
|---|---|---|---|
| 0 | 4 | `magic` | ASCII `RLAK` (`0x52 0x4C 0x41 0x4B`) |
| 4 | 1 | `status` | see below |
| 5 | 2 | `messageLength` | length of `message`, may be `0` |
| 7 | *messageLength* | `message` | UTF-8 diagnostic text, not for display |

| `status` | Meaning |
|---|---|
| `0x00` | `OK` — payload received in full and digest verified |
| `0x01` | `DIGEST_MISMATCH` — payload received but the digest did not match |
| `0x02` | `STORAGE_ERROR` — receiver could not store the payload |
| `0x03` | `REJECTED` — receiver declined before or during transfer |

**`0x00` is the only status that completes the transfer.** It is what permits the sender to
clear its change log, so a receiver MUST NOT send `0x00` unless the payload is fully
persisted and verified.

## 7. Failure handling

| Situation | Sender behaviour |
|---|---|
| Protocol id not supported by the sender | multistream-select refuses; report version mismatch |
| Stream closes before the acknowledgement | report an interrupted transfer; change log kept |
| Acknowledgement status ≠ `0x00` | report a failed transfer; change log kept |
| Acknowledgement malformed or `magic` wrong | treat as a failed transfer |
| User closes the session mid-transfer | close the stream; change log kept |

| Situation | Receiver behaviour |
|---|---|
| Header malformed | close the stream without reading a payload |
| Fewer than `payloadLength` bytes arrive | discard any partial file; send nothing |
| Digest mismatch | discard the file, send `0x01` |

Neither side retries automatically. A failed transfer is repeated by the user opening a new
session.

## 8. Security notes

- The Noise handshake authenticates both peers. The peer id the sender shows the user for
  approval is the identity that receives the bytes.
- Approval is per connection and is not remembered. A receiver cannot assume a previous
  approval still holds.
- The payload is the user's complete knowledge base. A receiver should store it no more
  openly than the user's own data.
- The sender's identity is stable, so a receiver may pin it and warn if it changes.
