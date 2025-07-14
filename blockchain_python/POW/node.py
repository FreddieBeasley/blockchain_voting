import json
from rsa import PublicKey, newkeys
import blockchain as bcn  # Assuming blockchain.py contains Blockchain, Transaction, and Block classes



class Node:

    def __init__(self, Id):
        self.id = Id
        self.wallets = {}
        self.peers = []
        self.public_key, self.private_key = self.create_wallet()
        self.blockchain = bcn.Blockchain(self)
        print(f"{self.id} : INITIALIZED")
    
    #Wallets
    def create_wallet(self):
        public_key, private_key = newkeys(512)

        self.wallets[bcn.stringify(public_key)] = 100
        for peer in self.peers:
            peer.wallets[bcn.stringify(public_key)] = 100

        return public_key, private_key
    
    def get_wallet(self, public_key):
        return self.wallets.get(bcn.stringify(public_key),0)

    #peers
    def connect_to_peer(self,peer):
        self.peers.append(peer)

        for public_key, balance in peer.wallets.items():
            if public_key not in self.wallets:
                    self.wallets[public_key] = balance
        
        for transaction in peer.blockchain.pending_transactions:
            if not transaction.validate_transaction(self.wallets):
                continue

            if transaction.id not in [tx.id for tx in self.blockchain.pending_transactions]:
                self.blockchain.pending_transactions.append(transaction)

        print(f"{self.id} connected to {peer.id}")

        #Automatic Two Way Peer Connection
        if self not in peer.peers:
            peer.connect_to_peer(self)


    #Transaction Sharing
    def create_transaction(self, receiver, amount):

        transaction = bcn.Transaction(self.public_key, receiver, amount)
        transaction.sign_transaction(self.private_key)

        if transaction.validate_transaction(self.wallets):
            self.blockchain.add_transaction(transaction,self.wallets)
            print(f"{self.id} : Transaction Validated and Appended")
        else:
            print(f"{self.id} : Transaction rejected")
    

        for peer in self.peers:
            peer.receive_transaction(transaction)
    
    def receive_transaction(self, transaction):
        if transaction.validate_transaction(self.wallets):
            self.blockchain.add_transaction(transaction,self.wallets)
            print(f"{self.id} : Transaction Validated and Appended")
        else:
            print(f"{self.id} : Transaction rejected")
        
    def mine_block(self):
        if self.blockchain.pending_transactions:
            print(f"{self.id} : MINING...")
            self.blockchain.mine_block(self.public_key,self.wallets)
            
            """
            for peer in self.peers:
                peer.receive_block(self.blockchain.chain[-1])
            """


            """
            self.consensus()
            """

        else:
            print(f"{self.id} : No Block to Mine")


    """
    def receive_block(self, block):
        if block.validate_block(self.wallets):
            self.blockchain.chain.append(block)
            print(f"{self.id} : Block Validated and Appended")
        else:
            print(f"{self.id} : Block rejected")
    """

    def consensus(self):
        longest_chain_holder = self
        longest_chain = len(self.blockchain.chain)

        for peer in self.peers:
            if not peer.blockchain.validate_blockchain(self.wallets):
                continue
            
            if len(peer.blockchain.chain) > longest_chain:
                longest_chain_holder = peer
                longest_chain = len(peer.blockchain.chain)
        
        if longest_chain_holder != self:
            self.blockchain.chain = longest_chain_holder.blockchain.chain
            self.blockchain.pending_transactions = longest_chain_holder.blockchain.pending_transactions
            self.wallets = longest_chain_holder.wallets
            #self.wallets = longest_chain.wallets
            print("✅ Consensus Achieved - Chain Updated")
        else:
            print("❌ No Valid Longer Chain Found")


# --------------------------------------
# 🚀 TESTING SECTION 🚀
# --------------------------------------


#INITIALIZING NODE OBJECTS
print("\n")
Node1 = Node("One")
Node2 = Node("Two")

#CONNECTING NODES
print("\n")
Node1.connect_to_peer(Node2)

#Transaction Creating
print("\n")
Node1.create_transaction(Node2.public_key, 8)
Node2.create_transaction(Node1.public_key, 3)

print("\n")
print(Node1.blockchain.pending_transactions)
print(Node2.blockchain.pending_transactions)

#Checking Balance
print(Node1.wallets[bcn.stringify(Node1.public_key)])
print(Node2.wallets[bcn.stringify(Node2.public_key)])


#Creating Block
print("\n&&----------MINING----------&&")
Node1.mine_block()
print("&&----------MINING----------&&")


#Checking Balance
print("\n&&----------Node1--Balances----------&&")
print(f"Node1: {Node1.wallets[bcn.stringify(Node1.public_key)]}")
print(f"Node2: {Node1.wallets[bcn.stringify(Node2.public_key)]}")
print("&&----------Node1--Balances----------&&")

print("\n&&----------Node2--Balances----------&&")
print(f"Node1: {Node2.wallets[bcn.stringify(Node1.public_key)]}")
print(f"Node2: {Node2.wallets[bcn.stringify(Node2.public_key)]}")
print("&&----------Node2--Balances----------&&")


print(Node1.blockchain.serialize_blockchain())
print("\n")
print(Node2.blockchain.serialize_blockchain())
print("\n")

Node2.consensus()

print(Node1.blockchain.serialize_blockchain())
print("\n")
print(Node2.blockchain.serialize_blockchain())
print("\n")

print(Node1.wallets[bcn.stringify(Node1.public_key)])
print(Node2.wallets[bcn.stringify(Node2.public_key)])


                



        
