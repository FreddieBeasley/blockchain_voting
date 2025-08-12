# 🗃️ Documentation – Data Directory (Voting resources.Blockchain Java App)

This document describes the purpose, format, and handling of all persistent data files used by the voting blockchain application. The `data/` directory serves as the storage layer for the blockchain, vote queue, voter registry, and known peer list.

> 📁 Directory: `data/`

---
## Note

Currently, this directory handles simple persistence only and has no cryptographic security such as encryption or signing

## 📦 Contents

| File Name              | Purpose                                                        |
|------------------------|----------------------------------------------------------------|
| `blockchain.json`      | Stores the current state of the blockchain in JSON format.     |
| `pendingVotes.txt`     | Stores unmined, submitted votes (one per line, serialized).    |
| `remainingVoters.txt`  | Contains public keys of eligible voters who haven’t voted.     |
| `knownPeers.txt`       | Stores a list of known peer IPs or hostnames (for networking). |

---

## 🧠 What This Directory Does

- ✅ Persists the blockchain state between application runs
- ✅ Maintains a list of votes not yet included in a block
- ✅ Tracks which voters are still eligible to vote
- ✅ Keeps a basic list of peer nodes for bootstrapping
- ❌ Does **not** validate or sanitize file contents (handled in logic layer)
- ❌ Does **not** store user credentials, vote content, or secrets directly

---

## 📁 File Format Details

### `blockchain.json`

- **Purpose**: Stores the full blockchain as an array of blocks.
- **Format**: JSON object containing block metadata and votes.
- **Sample**:
```json
[
  {
    "previousHash": "0000",
    "votes": [
      {
        "voter": "Base64EncodedPublicKey",
        "voteValue": 5,
        "signature": "Base64Signature"
      }
    ],
    "nonce": 1243,
    "hash": "xyz...",
    "timestamp": 1720000000000
  }
]
```

### `knownPeers.txt`

- **Purpose**: Stores a list of known peers. Each peer is on a separate line
- **Format**: { Host } ||| { Port } ||| { PublicKey }
- **Sample**:
```text
localhost|||12345|||-----RSA PUBLIC KEY-----
```

### `remainingVoters.txt`

- **Stores**: A publicKey on each line of a voter who has registered by not yet voted
- **Format**: PublicKey
- **Sample**: -
```text
----RSA PUBLIC KEY-----
```

### `pendingVotes.txt`

- **Purpose**: Stores a list of the votes that have been submitted but not yet added to a block. A vote is on a separate line
- **Format**: { Voter PublicKey } ||| { VoteValue } ||| { Signature }
- **SampleA**:
```text
----RSA PUBLIC KEY-----|||5|||----RSA SIGNATURE----
```
- **SampleB**:
```text
----RSA PUBLIC KEY-----|||2|||null
```

## Key Logical and Design Decisions

### `blockchain.json`

- Although the format of a block is always the same, the length of the block may vary depending on the number of votes stored in it. For this reason it is easier to read the blockchain from json format as a pose to line-by-line in txt format.
- It is unlikely that this file will be up to date when a peer reconnects and therefore the peer will likely adopt a later blockchain from another peer.
### `knownPeers.txt`

- Since the data of a known peer is strictly 3 fields it is easiest to read them line by line and to use string handling to separate the fields
### `remainingVoters.txt`


- Since the file only stores a single piece of data ( the voters public key ) it is easiest to read the public keys off stored line-by-line
### `pendingVotes.txt`

- Since the data of a vote is strictly 3 fields it is easiest to read them line-by-line and to sue string handling to separate the fields.