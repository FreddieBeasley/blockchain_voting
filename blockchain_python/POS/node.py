from rsa import newkeys
from collections import deque
import random
import blockchain as bcn

class Node:
     def __init__(self,server, Id):
          self.id = Id
          self.server = server
          self.public_key, self.private_key = self.create_wallet()
          self.blockchain = bcn.Blockchain(self)
          self.connect_to_server()

     #Wallets
     def create_wallet(self):
          public_key, private_key = newkeys(512)
          public_key_str = bcn.stringify(public_key)

          self.server.wallets[public_key_str] = 100
          return public_key, private_key
     
     def get_wallet(self):
          return self.server.wallets.get(bcn.stringify(self.public_key),0)
     
     #Server Connection
     def connect_to_server(self):
          self.server.add_node(self)
     
     #Stake
     def stake(self, amount):
          public_key_str = bcn.stringify(self.public_key)

          if amount > self.server.wallets[public_key_str]:
               return "Insufficient funds to make this stake"
          if amount <= 0:
               return "Unable to stake non-positive amount"
          
          self.server.wallets[public_key_str] -= amount
          self.server.stakes[public_key_str] += amount
          return f"{amount} has been staked"

     
     #This makes it possible to unstake before receiving you punishment for a malicous block 
     """
     def unstake(self, amount):
          public_key_str = bcn.stringify(self.public_key)

          if amount > self.server.stakes[public_key_str]:
               return "Withdrawal amount > staked amount"
          if amount <= 0:
               return "Unable to withdraw a non-positive amount"

          self.server.stakes[public_key_str] -= amount
          self.server.wallets[public_key_str] += amount
          return f"{amount} has been removed from stake"
     """

     def attempt_mining(self):
          validator = self.server.select_validator()
          if validator:
               print(f"Validator: {bcn.stringify(validator.public_key)}")
               return validator.blockchain.validate_new_block(self.public_key, self.server.wallets)
          return "No Validator selected"

     #Create Transactions
     def create_transaction(self, receiver, amount):
          transaction = bcn.Transaction(self.public_key, receiver, amount)
          transaction.sign_transaction(self.private_key)

          if transaction.validate_transaction(self.server.wallets):
               self.blockchain.add_transaction(transaction,self.server.wallets)
               print(f"{self.id} : Transaction Validated and Appended")
          else:
               print(f"{self.id} : Transaction rejected")

     def validate_last_block(self):
          last_block = self.blockchain.chain[-1]
          
          if last_block.validate_block():
               print("The last created block was valid")
               return True
          else:
               print("The last created block was invalid - broadcasted to server")
               self.server.check_last_block(self)


class Server:
     def __init__(self):
          self.nodes = [] #stores nodes
          self.wallets = {} #stores ammount of currency
          self.stakes = {} #stores amount staked
          self.punished = {} #stores times punished
          self.blacklist = [] #stores malicous nodes removed

          self.pending_transactions = deque() #stored transactions that need to be added to blocks
     
     def add_node(self, node):
          public_key_str = bcn.stringify(node.public_key)

          if node in self.nodes:
               print("Node already part of server")
               return False
          
          if public_key_str in self.blacklist:
               print("Node not added because it is blacklisted")
               return False
          
          self.nodes.append(node)
          self.wallets[public_key_str] = 100
          self.stakes[public_key_str] = 0
          self.punished[public_key_str] = 0
          return True
          
     def select_validator(self):
          #Ensure there are nodes that have staked
          if not self.stakes:
               print("No stakes found. Cannot select a validator.")
               return None

          #Ensures someone has staked
          total_stake = sum(self.stakes.values())
          if total_stake == 0:
               print("No one has staked. Cannot select a validator.")
               return None

          #Chooses the validator
          choices = []
          for node in self.nodes:
               pub_key_str = bcn.stringify(node.public_key)
               weight = self.stakes.get(pub_key_str, 0)
               choices.extend([node] * weight)

          return random.choice(choices) if choices else None

     def punish_validator(self, validator, checker_node):
          validator_key_str = bcn.stringify(validator.public_key)
          checker_node_key_str = bcn.stringify(checker_node.public_key)

          if self.stakes.get(validator_key_str, 0) == 0:
               print(f"Validator {validator_key_str} has no stake to remove.")
               return
          
          penalty = (self.stakes[validator_key_str] * 0.2)
          self.stakes[validator_key_str] -= penalty

          #redistribute currency
          self.wallets[checker_node_key_str] += penalty
          
          #Checks is node needs to be removed
          self.punished[validator_key_str] += 1
          if self.punished[validator_key_str] == 3:
               self.blacklist_node(validator)

          print(f"Validator {validator_key_str} has been punished! Lost {penalty:.2f} coins.")
     
     def blacklist_node(self, node):
          public_key_str = bcn.stringify(node.public_key)
          self.nodes.remove(node)
          self.wallets.pop(public_key_str)
          self.stakes.pop(public_key_str)
          self.punished.pop(public_key_str)
          self.blacklist.append(public_key_str)

     def check_last_block(self, checker_node):

          count = 0
          for node in self.nodes:
               if node.validate_last_block():
                    continue
               count += 1
          
          if count >= 0.5 * len(self.nodes):
               print("last created block is deamed invalid")
          
          validator = checker_node.blockchain.node[-1].validator
          self.punish_validator(validator, checker_node)


# --------------------------------------
# 🚀 TESTING SECTION 🚀
# --------------------------------------

Mainserver = Server()

Node1 = Node(Mainserver,"Node1")
Node2 = Node(Mainserver,"Node2")

#Print Initial Wallets
print("\n🔍 Initial Wallet Balances:")
for node in Mainserver.nodes:
    print(f"Node {bcn.stringify(node.public_key)}: {Mainserver.wallets[bcn.stringify(node.public_key)]} coins")

#Checking Balance
print("\n⚖️Balances⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)])
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")

#Create transactions
Node1.create_transaction(Node2.public_key, 8)
Node2.create_transaction(Node1.public_key, 3)

#Checking Balance
print("\n⚖️Balances⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)])
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")


print(Mainserver.pending_transactions)

# Staking tests
print("\n💰 Staking Tests:")
print(Node1.stake(50))
print(Node2.stake(30))
print(Node1.stake(200))

#Checking Balance
print("\n⚖️Balances⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)])
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")

# Validator selection
print(Node1.attempt_mining())

print("\n⚖️Balances⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)])
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")

print("\n⚖️Balances - rewards⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)] - 5)
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")

print(Node2.attempt_mining())

#Checking Balance
print("\n⚖️Balances⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)])
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")

print("\n⚖️Balances - rewards⚖️")
print(Mainserver.wallets[bcn.stringify(Node1.public_key)] - 5)
print(Mainserver.wallets[bcn.stringify(Node2.public_key)])
print("⚖️--------⚖️\n")

