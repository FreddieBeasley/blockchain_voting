# Blockchain Voting System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)

---

## Overview of Decentralized Ledger Technology for Voting Systems

This project implements a digital voting system using a decentralized ledger to record votes securely, transparently, and tamper-resistantly. Below is an explanation of its functionality:

### How It Works
- **Immutable Records**: Votes are stored in a distributed digital ledger across multiple devices. Each vote is part of a data set (block), cryptographically linked to the previous block using hash values, ensuring an unchangeable record.
- **Key Features**:
  - **Immutability**: Each vote, consisting of a voter's RSA public key, vote value, and RSA signature, is permanently recorded and cannot be altered without modifying all subsequent blocks across the network.
  - **Transparency**: The public ledger allows anyone to verify vote counts, ensuring trust while anonymizing voter identities for privacy.
  - **Distribution**: The ledger is replicated across multiple nodes (computers), preventing any single point of failure or manipulation.

### System Basics
Votes are stored in a distributed digital ledger maintained across multiple devices. Each block of vote data is linked to the previous block through cryptographic hash values, forming a secure, immutable chain of records. This structure ensures:
- Protection against tampering.
- Decentralized control with no single authority.
- Accessible verification for all participants, fostering trust.

---

## Project Structure

The project is organized into Python and Java components, with the following structure:

```
.
├── blockchain_python
│   ├── POS
│   │   ├── __pycache__
│   │   │   └── blockchain.cpython-313.pyc
│   │   ├── blockchain.py
│   │   ├── node.py
│   │   └── requirements.txt
│   ├── POW
│   │   ├── __pycache__
│   │   │   └── blockchain.cpython-313.pyc
│   │   ├── blockchain.py
│   │   ├── node.py
│   │   ├── requirements.txt
│   │  
│   └── voting_blockchain_python
│       ├── app.py
│       ├── blockchain_simulation.json
│       ├── blockchain.py
│       ├── gen_keys.py
│       ├── rsa2.py
│       └── voter_keys.json
│ 
└── voting_blockchain_java_app
    ├── MyApp.iml
    ├── out
    │   └── production
    │       └── blockchain-app
    │           ├── blockchain
    │           │   └── DOCUMENTATION.md
    │           └── network
    │               ├── DOCUMENTATION.md
    │               └── network.txt
    ├── pom.xml
    ├── README.md
    └── src
        ├── main
        │   ├── data
        │   │   ├── blockchain.json
        │   │   ├── network_manager.json
        │   │   └── registered_voters.json
        │   ├── data2
        │   │   ├── blockchain.json
        │   │   ├── network_manager.json
        │   │   └── registered_voters.json
        │   ├── frontend
        │   │   ├── owner
        │   │   │   ├── blockchain.html
        │   │   │   ├── log.html
        │   │   │   ├── network.html
        │   │   │   └── root.html
        │   │   ├── user
        │   │   │   └── vote.html
        │   │   └── vote2.html
        │   ├── java
        │   │   ├── app
        │   │   │   ├── api
        │   │   │   ├── documentation
        │   │   │   │   └── DirectoryInfo.txt
        │   │   │   ├── LocalNode.java
        │   │   │   └── resources
        │   │   │       ├── blockchain
        │   │   │       │   ├── Chain.java
        │   │   │       │   ├── PendingVotes.java
        │   │   │       │   ├── RemainingVoters.java
        │   │   │       │   └── resources
        │   │   │       │       ├── Block.java
        │   │   │       │       └── Vote.java
        │   │   │       ├── Blockchain.java
        │   │   │       ├── ControlServer.java
        │   │   │       ├── exceptions
        │   │   │       │   ├── ArchivedException.java
        │   │   │       │   ├── InvalidException.java
        │   │   │       │   ├── LoadException.java
        │   │   │       │   ├── MalformedJSONException.java
        │   │   │       │   ├── MaliciousException.java
        │   │   │       │   ├── OverflowException.java
        │   │   │       │   └── PersistenceException.java
        │   │   │       ├── JSONParsers
        │   │   │       │   ├── BlockchainParser.java
        │   │   │       │   └── NetworkParser.java
        │   │   │       ├── logging
        │   │   │       │   └── LogAppender.java
        │   │   │       ├── network
        │   │   │       │   ├── KnownPeers.java
        │   │   │       │   ├── MessageCache.java
        │   │   │       │   ├── MessageReceiver.java
        │   │   │       │   ├── messages.json
        │   │   │       │   ├── MessageSender.java
        │   │   │       │   └── resources
        │   │   │       │       └── RemotePeer.java
        │   │   │       ├── NetworkManager.java
        │   │   │       ├── Persistence.java
        │   │   │       ├── util
        │   │   │       │   ├── Cryptography.java
        │   │   │       │   ├── Exceptions.java
        │   │   │       │   └── FileHandlers.java
        │   │   │       └── WebServer.java
        │   │   ├── TestingNode1.java
        │   │   └── TestingNode2.java
        │   └── resources
        │       └── logback.xml
        └── test
            └── java
```



---

## Architecture

This project employs the **Mediator Pattern** for efficient coordination between components. The `LocalNode.java` class acts as the primary mediator, orchestrating the `/network`, `/blockchain`, and server subsections. Advanced subsections like `/network` and `/blockchain` include secondary mediators (`NetworkManager.java` and `Blockchain.java`, respectively) to manage their specific functions.

---

### Glossary

To ensure clarity, this README uses specific terminology that may differ from standard conventions. Below are the definitions of key terms:

- **Host**: Outside networking contexts (e.g., host:port), refers to the node owner with full control over the blockchain and codebase access.
- **User**: A client interacting with the blockchain, typically to vote or register.
- **Mediator**: A class that centralizes communication by coordinating all sub-mediators within a system.
- **Primary Mediator**: The central coordinator for the entire project, e.g., `LocalNode.java`.
- **Secondary Mediator**: A coordinator for a specific subsection, e.g., `NetworkManager.java` for network classes or `Blockchain.java` for blockchain classes.

---

### Blockchain

The `blockchain` directory manages core blockchain logic, ensuring transparency, immutability, proof of work, and consensus.

- **`Blockchain.java`**: Mediates blockchain subclasses, coordinating `Chain.java`, `PendingVotes.java`, and `RemainingVoters.java`.
- **`Chain.java`**: Stores an immutable sequence of blocks containing votes, with methods for mining and validation.
- **`PendingVotes.java`**: A queue of votes known to the blockchain but not yet validated or added to a block.
- **`RemainingVoters.java`**: A list initialized with the same voter IDs as *Registered Voters*, with IDs removed once a voter’s vote is validated and added to the blockchain.

> **Note**: Currently, the `Chain.java`, `RemainingVoters.java`, and `PendingVotes.java` functionalities are integrated within `Blockchain.java` and are not separate classes.

---

### Network

The `network` directory handles communication between peers:

- **`NetworkManager.java`**: Mediates networking functions, coordinating related classes and managing message creation and interpretation between nodes.
- **`MessageSender.java`**: Opens a client socket to connect to a peer’s server socket, sends a message, and awaits a response.
- **`MessageReceiver.java`**: Opens a server socket when `NetworkManager.java` starts, listening for incoming client connections and handling messages in a separate thread.
- **`KnownPeers.java`**: Maintains a set of connected peer nodes, including the maximum allowable connections and utility methods.
- **`MessageCache.java`**: Stores a queue of message hashes to prevent reprocessing duplicate messages.
- **`RemotePeer.java`**: A data structure storing peer details, including RSA public key, host, and port.

---

### Webserver

The web server, hosted on a local port, enables user registration and voting:

- **Registration**: Generates a public-private key pair using JavaScript, returned to the user and sent to the blockchain for inclusion in *registered_voters* and *remaining_voters*.
- **Voting**: Validates the submitted public-private key pair, creates a signature with the private key, and converts the public key, vote value, and signature into a vote object in the backend, sent to the blockchain for inclusion in *pending_votes*.

---

### ControlServer

The control server, running on a local port, enhances blockchain functionality for the host via a control panel with the following features:

- View all logs generated by the code.
- Display the entire blockchain in JSON format with live updates.
- List all peers connected to the node.
- Connect to new peers via a form accepting host and port inputs.

---

### Persistence

The `Persistence.java` class serves as a helper, managing the storage directory and handling the saving and loading of `Blockchain.java` and `NetworkManager.java` classes.

---

### Utils

The `util` directory isolates frequently used logic for reusability, including:

- **File Handling**: Methods for reading, writing, and appending files.
- **JSON Parsers**: Tools for converting between data structures (e.g., blocks) and JSON format.
- **Encryption**: Functions for generating RSA keys, creating/verifying RSA signatures, and other cryptographic operations.
- **Exception Formatting**: A method for formatting exceptions to aid debugging.

---

## Design Decisions and Thoughts

### How to Use

The project is currently in a testing state, as evidenced by the two main files that load two nodes for testing, along with two data directories for data persistence. To test the blockchain, I recommend running both main files (in IntelliJ IDEA, no manual compilation is needed). Then, open the control servers in your browser using the URL `http://localhost:port`, where `port` is the port number you specify. Here, you can view logs, the blockchain, and use the form to connect the two nodes. Similarly, use the web server to register to vote, submit votes, and observe data transfer between nodes via logs and blockchain updates.

To deploy the blockchain properly, I would remove testing elements like the second data directory and distribute the compiled project or a zip file to potential nodes, allowing them to run a single node on their local device.

---

### What I Would Do Next

1. **`KnownPeers.java` Enhancement**: `KnownPeers.java` stores a list of peers a node is aware of, along with a `max_peers` field to limit connections based on the node’s computational capacity, preventing slowdowns. Currently, if a peer goes offline, the node detects the failed connection but takes no action. If all peers go offline and `KnownPeers` is full, the node could become permanently disconnected from the blockchain network. To address this, I suggest implementing `KnownPeers` as a queue. When a node receives a message from a peer, it should remove the peer from `KnownPeers` and re-add it to the back of the queue. A new thread should periodically (e.g., every minute) ping the peer at the front of the queue. If there’s no response, the peer should be removed from `KnownPeers`, allowing new connections and preventing permanent disconnection.

2. **Refactor `Blockchain.java`**: Currently, `Blockchain.java` handles multiple tasks. I suggest making it a secondary mediator coordinating subclasses like `Chain.java`, `PendingVotes.java`, and `RemainingVoters.java`, as outlined in the architecture section.

3. **Network Message Handling**: `NetworkManager.java` currently handles both message forging and interpretation, as well as coordination. I propose creating two new classes, `MessageHandling.java` and `MessageForger.java`, to separate the logic for creating and interpreting peer messages.

4. **Database for Persistence**: `Persistence.java` currently saves data (peers, blockchain) to JSON files in the data directory. I suggest using a NoSQL database like MongoDB, which can store data in JSON format but offers greater security.

5. **Blacklist Implementation**: I would implement a `Blacklist.java` class to store data on nodes deemed malicious. A strikes map could track node behavior, transferring nodes to the blacklist after three strikes. Blacklisted nodes would be immediately removed from `KnownPeers`. The blacklist would only need checking for incoming connection messages, as blacklisted nodes cannot be in `KnownPeers`, and their messages would be discarded.

6. **Connection Check**: When a node sends an outgoing connection request, it should verify that `KnownPeers` has space for the new connection. Currently, a node could attempt a connection it cannot add, leading to a one-way connection. This would be resolved by the ping mechanism mentioned above.

7. **Message Hash Verification**: Nodes should verify the hash provided in a message. Alternatively, since nodes generate a hash to check against `MessageCache`, messages could omit the hash, and nodes could generate it directly, reducing workload while maintaining integrity.

8. **Voter Registration Security**: Currently, users and hosts can create voters freely. In a real election, voter ID (e.g., passport or voter ID card) or credentials (e.g., email/password for a school election) should be required. However, hosts could still create multiple voters and distribute them across the network, which is hard to flag as malicious. To ensure security, a separate registration process could store voter public keys on a centralized, secure server. During voting, nodes would verify voter IDs against this server and check for duplicate votes. While this reduces decentralization and introduces a single point of failure, it’s the most secure approach to prevent fraudulent voting.

9. **Multi-Device Testing**: Testing should occur across multiple devices, but I only had access to one, so I used different ports for nodes.

10. **Additional Improvements**: I considered other improvements during development but cannot recall them all at this time.

---

### Specific Design Choices

1. **Helper Classes**: I implemented `JSONParsers`, `FileHandling`, `ExceptionFormatting`, and `Cryptography` as helper classes to isolate reusable logic and ensure consistency across the project.

2. **Message Handling**: Initially, nodes sent messages and closed connections, with peers opening new connections to respond. This was simple but unintuitive (e.g., a “blockchain request” wasn’t a true request but a notification). The current implementation, where nodes await responses, is more intuitive and efficient.

3. **Proof of Work**: For a voting blockchain, proof of work is the most suitable consensus mechanism. I considered proof of stake, which punishes malicious nodes by deducting funds, but deemed it unsuitable, as bad actors might willingly spend money to manipulate elections.

4. **Mediator Pattern**: I chose the Mediator Pattern with `LocalNode.java` as the central coordinator for communication between `WebServer.java`, `ControlServer.java`, `Blockchain.java`, and `NetworkManager.java`. I considered a circular architecture where these components communicate directly, but as I was already implementing the Mediator Pattern, I continued with it. I would consider switching to a circular architecture if research showed it to be more efficient.

5. **Java Over Python**: I initially started in Python (see `blockchain_python` directory) but switched to Java for its object-oriented capabilities and performance benefits. While Python could support OOP, research indicated Java was better suited for this project, so I taught myself Java.


