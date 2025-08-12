# 📘 Documentation – Network Module (Voting resources.Blockchain Java App)

This document provided **in-depth technical documentation** for the `network/` module, which implements the core functionality of a peer on a network communicating with other peer regarding blockchain updates in java.

This module assumes responsibility for a peer-to-peer node regarding the persistent knownNode storage, Parser methods between JSON and Object format as well as Client and Server node functionality.

---

## 🧱 Class Responsibilities

### `Client.java`
- **Purpose**: Handles the sending of messages  to other peers on the network
- **Fields**:
- **Key Methods**:

### `Server.java`
- **Purpose**: Handles the receiving of messages from other nodes on the network
- **Fields**:
- **Key Methods**:

### `MessageFactory.java`
- **Purpose**: Convert java Objects to Json with the help of the `messageHanding/` Parser classes and methods which can be sent across the network.
- **Fields**:
- **Key Methods**:

### `MessageHandler.java`
- **Purpose**: Incoming Json messages back to their respective Java objects before adding them to their relevant blockchain location:
  - Valid blocks appended to chain
  - Invalid blocks discarded
  - Votes added to pending votes
  - Peers added to knownPeers if knownPeers is not at capacity
- **Fields**:
- **Key Methods**:

### `knownPeers.java`
- **Purpose**: Non-persistent easy access list of RemotePeer objects which the LocalPeer is aware of. Directly updates and reads from the persistent knownPeers.txt
- **Fields**: 
- **Key Methods**:

### `messageHandling/`
- **Contents**:
  - BlockMessageParser
    - **Purpose**: Converting Block Objects to Json and vice versa.
    - **Fields**: None
    - **Key Methods**: JSONToBlock, BlockToJSON
  - RemotePeerMessageParser
    - **Purpose**: Converting Block Objects to Json and vice versa.
    - **Fields**: None
    - **Key Methods**: JSONToPeer, PeerToJSON
  - VoteMessageParser
    - **Purpose**: Converting Block Objects to Json and vice versa.
    - **Fields**: None
    - **Key Methods**: JSONToVote, VoteToJSON

### `nodes/`
- **Contents**:
  - Peer
    - **Purpose**: A superclass that contains the fields and methods that overlap LocalPeer and RemotePeer objects
    - **Fields**: Host, Port, PublicKey
    - **Key Methods**: toString, equals
  - LocalPeer ( extends Peer )
    - **Purpose**: A subclass referring to the local peer on the network ( self )
    - **Fields**: PrivateKey ( used to sign messages )
    - **Key Methods**: signMessage
  - RemotePeer (extends Peer )
    - **Purpose**: A subclass referring to other peers on the network
    - **Fields**: None
    - **Key Methods**: None
- **Information**:
  - LocalPeer generate public and private key pair on initialisation
  - RemotePeer except public key as a parameter in initialisation
  - Both receive Host and Port as a parameter in initialisation


## Message Format Details

### Block Message
```json

    {
        "type": "NEW_BLOCK",
        "data": {
            "hash": "BLOCK_HASH",
            "previous_hash": "PREVIOUS_HASH",
            "timestamp": "TIMESTAMP",
            "nonce": "NONCE",
            "votes": []
        },
        "sender_node": "SENDER_PUBLIC_KEY",
        "signature": "SIGNATURE"
    }

```
### Peer Message
```json
    {
        "type": "NEW_PEER",
        "data": {
            "host": "HOST",
            "port": "PORT",
            "public_key": "PUBLIC_KEY"
        },
        "sender_node": "SENDER_PUBLIC_KEY",
        "signature": "SIGNATURE"
    }
```
### Vote Message
```json
 {
        "type": "NEW_VOTE",
        "data": {
            "voter": "VOTER_PUBLIC_KEY",
            "data": "VOTE",
            "signature": "SIGNATURE"
        },
        "sender_node": "SENDER_PUBLIC_KEY",
        "signature": "SIGNATURE"
    }
```

---

## 📚 Related Files

- `util/CryptographyUtils.java` – handles key serialization/deserialization
- `blockchain/` – chains the blocks ( which contain votes ) together to form the actual blockchain

---

## 🧑‍💻 Author

**Freddie Beasley**  
2025



