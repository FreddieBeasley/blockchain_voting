# 📘 Documentation – resources.Blockchain Module (Voting resources.Blockchain Java App)

This document provides **in-depth technical documentation** for the `blockchain/` module, which implements the core functionality of a cryptographically secure, vote-based blockchain system in Java.

This module assumes responsibility for vote validation, block mining, blockchain integrity, and internal memory management. It does **not** manage networking, user interfaces, or robust storage systems — those are handled in other parts of the application.

---

## 🧱 Class Responsibilities

### `Vote.java`
- **Purpose**: Encapsulates a voter's choice and cryptographic signature.
- **Fields**:
   - `PublicKey voter` – identifies the voter uniquely.
   - `int voteValue` – candidate ID or vote weight.
   - `String signature` – digital signature of the vote.
- **Key Methods**:
   - `signVote(PrivateKey)` – signs vote data.
   - `isValid()` – verifies the vote using the voter's public key.
   - `serialise()` – encodes the vote to a string for storage or transmission.

### `Block.java`
- **Purpose**: Holds a batch of votes and links to the blockchain.
- **Fields**:
   - `List<Vote> votes`
   - `String previousHash`
   - `String hash`
   - `long timestamp`
   - `int nonce`
- **Key Methods**:
   - `computeHash()` – returns the SHA-256 hash of the block contents.
   - `mineBlock(int difficulty)` – performs proof-of-work.
   - `isValid(int difficulty)` – checks block hash, proof-of-work, and vote validity.

### `resources.Blockchain.java`
- **Purpose**: Manages the sequence of blocks, pending votes, and voter eligibility.
- **Fields**:
   - `List<Block> chain` – the main blockchain.
   - `PendingVotes pendingVotes` – vote queue.
   - `RemainingVoters remainingVoters` – registry of who has not voted.
   - `int difficulty` – number of leading zeros required for a valid block hash.
- **Key Methods**:
   - `addNewVote(Vote)` – queues a new vote.
   - `createNewBlock()` – builds and mines a block from pending votes.
   - `isValid()` – validates the integrity of the entire chain.

### `PendingVotes.java`
- **Purpose**: Temporary storage for votes awaiting inclusion in a block.
- **Implements**: Queue semantics using `LinkedList`.
- **resources.Persistence**: Appends serialized vote strings to `pendingVotes.txt`.

### `RemainingVoters.java`
- **Purpose**: Prevents double voting by tracking voters who haven't voted.
- **Structure**: Uses a `HashSet<PublicKey>` to store voter keys.
- **resources.Persistence**: Updates remainingVoters.txt once a voter has voted
- **Key Method**: `removeVoter(PublicKey)` returns `true` if the voter was valid and removed.

---

## 🔄 Vote Lifecycle

1. **Vote Created**:
   - A voter signs their vote with their private key.
   - A `Vote` object is created and added to `PendingVotes`.

2. **Vote Queued**:
   - The serialized vote is stored in `pendingVotes.txt`.

3. **Block Created**:
   - `resources.Blockchain.createNewBlock()` collects votes, validates them, and mines a block.
   - Valid votes are removed from `RemainingVoters`.

4. **Block Verified & Added**:
   - Block is verified (`hash`, `nonce`, and each vote).
   - Block is added to the chain if valid.

---

## 🔐 Cryptography Overview

- **Signing Algorithm**: RSA with SHA-256
- **Vote Signing**:
   - Data signed: `Base64(publicKey) + voteValue`
   - Signature generated using voter's private key
- **Verification**:
   - Signature is verified using public key
- **Block Hashing**:
   - Includes: `previousHash + timestamp + vote data + nonce`
   - Hashing Algorithm: `SHA-256`, Base64-encoded

---

## ⚙️ Proof-of-Work

- **Difficulty**: Fixed at 4 (i.e. hash must start with `"0000"`) for testing purposes
- **Mining**: Iteratively increment `nonce` until valid hash is produced

---

## 📁 File Format Details

### Vote Serialization Format
- pendingVotes.txt
  - Stores a single vote on each line
  - Stored data includes "Voter" ( Public Key ), "VoteValue" ( int ), and "signature" string
  - Stored data is converted to strings and seperated by a triple-pide ( "|||" )
- remainingVoters.txt
  - Stores a single voter public key on each line in string format
  - Allows only for removal
- blockchain.json
  - Stores the blockchain information as a JSONArray ( chain ) of JSONObjects ( blocks ) containing JSONArrays ( lists ) of JSONObjects ( votes ).

---

## 📌 Limitations

- Only basic file-based persistence (e.g. no recovery from file into memory on startup)
- Not fault-tolerant if system crashes mid-block creation
- No Merkle root or advanced cryptographic summaries of block content

---

## 🧪 Currently Working On

- ✅ Load `RemainingVoters`, `PendingVotes`, and `blockchain` from disk on startup
- ✅ Store `resources.Blockchain` to disk in a JSON or binary format
- 🧠 Implement Merkle tree support for block content
- 🌐 Sync chain across networked peers
- 🔒 Add encrypted storage for votes and chain data

---

## 📚 Related Files

- `util/CryptographyUtils.java` – handles key serialization/deserialization
- `network/` – handles peer communication (not part of this module)

---

## 🧑‍💻 Author

**Freddie Beasley**  
2025