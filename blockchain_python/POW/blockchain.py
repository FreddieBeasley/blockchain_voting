#modules
import rsa
import json
from collections import deque
from hashlib import sha256
from time import time


#Blockchain Logic
class Transaction:
     def __init__(self, sender_public_key, receiver_public_key, amount):
          self.id = f"{sender_public_key},{receiver_public_key},{amount}"
          self.sender = sender_public_key
          self.receiver = receiver_public_key
          self.amount = amount
          self.signature = None
     
     def sign_transaction(self, private_key):
          data = f"{self.sender}{self.receiver}{self.amount}"
          self.signature = rsa.sign(data.encode("utf-8"), private_key,"SHA-256")
     
     def validate_transaction(self, wallets):

          sender_public_key_str = stringify(self.sender)
          receiver_public_key_str = stringify(self.receiver)

          if self.amount <=0:
               print("Failure to validate: Invalid amount")
               return False
          
          if sender_public_key_str not in wallets:
               print("Failure to validate: Sender wallet does not exist")
               return False
          
          if receiver_public_key_str not in wallets:
               print("Failure to validate: Receiver wallet does not exist")
               return False

          if self.amount > wallets[sender_public_key_str]:
               print("Vailure to validate: Sender has insufficient funds")
               return False
          
          if not self.signature:
               print("Failure to validate: Missing signature")
               return False

          try:
               data = f"{self.sender}{self.receiver}{self.amount}"
               rsa.verify(data.encode("utf-8"), self.signature, self.sender)
          except rsa.VerificationError:
               print("Failure to validate: Signature incorrect")
               return False
          
          return True

     def serialize_transaction(self):
          return {
               "sender": self.sender.save_pkcs1().decode("utf-8"),
               "receiver": self.receiver.save_pkcs1().decode("utf-8"),
               "amount": self.amount,
               "signature": self.signature.hex() if self.signature != None else None
          }

class Block:
     def __init__(self, transactions, previous_hash="0000"):
          self.transactions = transactions
          self.timestamp = time()
          self.nonce = 0
          self.previous_hash = previous_hash
          self.hash = self.proof_of_work()
     
     def proof_of_work(self):
          difficulty = 2

          self.hash = self.calculate_hash()
          while self.hash[:difficulty] != "0"*difficulty:
               self.nonce += 1
               self.hash = self.calculate_hash()
          return self.hash
     
     def calculate_hash(self):
          transaction_str = ""

          for transaction in self.transactions:
               transaction_str += json.dumps(transaction.serialize_transaction(),sort_keys=True)
          data = str(self.nonce) + str(self.timestamp) + str(self.previous_hash) + transaction_str
          
          return sha256(data.encode("utf-8")).hexdigest()
     
     def validate_block(self,wallets):
          difficulty = 2
          if self.hash[:difficulty] != "0"*difficulty:
               print("No Proof of Work")
               return False
          
          for transaction in self.transactions:
               if not transaction.validate_transaction(wallets):
                    print(f"a transaction could not be validated")
                    return False
          
          return True

     def serialize_block(self):
          return {
          "transactions": [transaction.serialize_transaction() for transaction in self.transactions],
          "timestamp": self.timestamp,
          "nonce" : self.nonce,
          "previous_hash": self.previous_hash,
          "hash": self.hash
          }

class Blockchain:
     def __init__(self,node):
          self.chain = [Block([])]
          self.pending_transactions = deque()
          self.peer_nodes = set()
          self.public_key, self.private_key = node.public_key, node.private_key

     def add_transaction(self, transaction, wallets):
          if not transaction.validate_transaction(wallets):
               print("Transaction could not be added because it is invalid")
               return False

          wallets[stringify(transaction.sender)] -= transaction.amount

          self.pending_transactions.append(transaction)
          return True

     def mine_block(self, miner_public_key, wallets):
          if len(self.pending_transactions) == 0:
               return False

          transactions_to_add = []

          while len(self.pending_transactions) != 0:
               transaction = self.pending_transactions.popleft()

               sender_public_key_str = stringify(transaction.sender)
               receiver_public_key_str = stringify(transaction.receiver)

               if transaction.validate_transaction(wallets):
                    wallets[receiver_public_key_str] += transaction.amount
                    transactions_to_add.append(transaction)
               else:
                    wallets[sender_public_key_str] += transaction.amount
          
          #reward handling
          reward_amount = 5
          mining_reward_transaction = Transaction(self.public_key, miner_public_key, reward_amount)
          mining_reward_transaction.sign_transaction(self.private_key)
          wallets[stringify(miner_public_key)] += reward_amount

          transactions_to_add.append(mining_reward_transaction)
          new_block = Block(transactions_to_add, self.chain[-1].hash)

          self.chain.append(new_block)
          self.pending_transactions.clear()

          print("✅ Block successfully mined & reward given!")
          return True
          
     def validate_blockchain(self,wallets):
          for i in range(1,len(self.chain)):
               previous_block = self.chain[i - 1]
               current_block = self.chain[i]

               if not current_block.validate_block(wallets):
                    return False
               
               if previous_block.calculate_hash() != current_block.previous_hash:
                    return False
          
          return True


     def serialize_blockchain(self):
          for block in self.chain:
               print(block.serialize_block())

#Wallets

'''
def stringify(key): #changes keys in strings
     return key.save_pkcs1().decode("utf-8")
'''

def stringify(key):
     key_pem = key.save_pkcs1()
     key_hash = sha256(key_pem).hexdigest()
     return key_hash