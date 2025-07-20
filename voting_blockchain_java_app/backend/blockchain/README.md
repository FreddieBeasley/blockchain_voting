The blockchain directory hold the key logic and data for the blockchain.

Key logic and data includes:

Structur
1. Votes - The "Vote.java" class holds defines the structures and behaviour of a vote. It contains fields such as "voter", "voteValue" and "signature". It also stores key methods including "sign" - which takes a PrivateKey as a parametre and provides a digital signature , and "isValid" which verifys the signature
