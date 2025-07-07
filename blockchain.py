#imports
import json
import rsa
from hashlib import sha256
from time import time
from collections import deque

class Voter:
     def __init__(self, voter_id):
          self.__voter_id = voter_id
          self.__public_key, self.__private_key = rsa.newkeys(512)

     def sign_vote(self, vote):
          vote.sign(self.__private_key)


     def get_public_key(self):
          return self.__public_key



class Vote:
     def __init__(self, voter_public_key):
          self.__voter = voter_public_key
          self.__vote = self.assign_vote() 
          self.__signature = None

     #getters
     def get_voter(self):
          return self.__voter
     
     def get_signature(self):
          return self.__signature
     
     def get_vote(self):
          return self.__vote

     
     def assign_vote(self):
          vote = ""
          while vote not in ["1","2","3","4","5"]:
               print(
                    "\n"
                    "1 : Conservatives\n",
                    "2 : Reform\n",
                    "3 : Labour\n",
                    "4 : LibDems\n",
                    "5 : Green\n",
               )
               vote = input("Input a number: ")
          
          return int(vote)
     

     def sign(self, private_key):
          data = f"{self.get_voter().save_pkcs1().decode("utf-8")}{self.get_vote()}"
          self.__signature = rsa.sign(data.encode("utf-8"), private_key, "SHA-256")

     def validate_vote(self):
          # Checking for signature
          if not self.__signature:
               print(f"{self.__voter} | {self.__vote}: Missing Signature")
               return False
          
          # Verifying signature
          data = f"{self.__voter.save_pkcs1().decode()}{self.__vote}"

          try:
               rsa.verify(data.encode(), self.__signature, self.__voter)
               return True
          
          except rsa.VerificationError:
               print(f"Invalid signature for vote by {self.__voter}")
               return False

     def serialise(self):
          return {
               "voter": self.__voter.save_pkcs1().decode("utf-8"),
               "vote": str(self.__vote),
               "signature": self.__signature.hex() if self.__signature is not None else None
          }
     
class Block:
     def __init__(self, votes:list, previous_hash="0000"):

          #Vote Tracking
          self.__miner = {}
          self.__vote_count = {}
          self.__votes = votes

          #Hashing & Validation
          self.__difficulty = 2
          self.__timestamp = time()
          self.__nonce = 0
          self.__previous_hash = previous_hash
          self.__hash = self.__proof_of_work()


     # Getter methods
     def get_timestamp(self):
          return self.__timestamp
     
     def get_previous_hash(self):
          return self.__previous_hash
     
     def get_hash(self):
          return self.__hash
     
     # Setter methods
     def set_miner(self, miner_id:str):
          self.__miner = miner_id

     # Special methods
     def __proof_of_work(self):
          hash = self.__calculate_hash()

          while hash[:self.__difficulty] != "0"*self.__difficulty:
               self.__nonce +=1
               hash = self.__calculate_hash()

          return hash
          
     def __calculate_hash(self):
          vote_str = ""

          for vote in self.__votes:
               vote_str += json.dumps(vote.serialise(), sort_keys=True)

          data = f"{self.__nonce}{self.__timestamp}{self.__previous_hash}{vote_str}"
               
          return sha256(data.encode("utf-8")).hexdigest()
     
     def validate_block(self):
          if self.__hash[:self.__difficulty] != "0"*self.__difficulty:
               print("No Proof of Work")
               return False
          
          if self.__hash != self.__calculate_hash():
               print("Incorrect hash")
               return False

          for vote in self.__votes:
               if not vote.validate_vote():
                    return False

          return True

     def serialise(self):
          return {
               "votes": [vote.serialise() for vote in self.__votes],
               "timestamp": self.__timestamp,
               "nonce" : self.__nonce,
               "previous_hash": self.__previous_hash,
               "hash": self.__hash
          }

class Blockchain:
     def __init__(self):
          self.__chain = [Block(votes=[])] #of Blocks
          self.__pending_votes = deque()
          self.__voted_voters = set()
     
     # Getter methods
     def get_chain(self):
          return self.__chain
     
     # Special Methods
     def add_vote(self, vote:Vote):
          if vote.get_voter() in self.__voted_voters:
               print("Voter has already voted")
               return False

          if not vote.validate_vote():
               return False

          #fix to remove not add voters
          self.__pending_votes.append(vote)
          self.__voted_voters.add(vote.get_voter())

          return True
     
     def mine_block(self):
          if len(self.__pending_votes) == 0:
               print("No pending votes")
               return False

          votes_to_add = list(self.__pending_votes)
          self.__pending_votes.clear()
          
          new_block = Block(votes=votes_to_add, previous_hash=self.__chain[-1].get_hash())
          self.__chain.append(new_block)

          print("Block successfully mined")

          return True

     def validate_blockchain(self):
          for i in range(1, len(self.__chain)):
               if len(self.__chain) > 1:
                    previous_block = self.__chain[i - 1]
                    current_block = self.__chain[i]
                    
                    if previous_block.get_hash() != current_block.get_previous_hash():
                         print(f"Invalid blockchain: chain is broken between block {i-1} and {i}")
                         return False
                    
               if not current_block.validate_block():
                    return False
          
          return True

     def serialise(self):
          for block in self.__chain:
               print(block.serialise())


import random

def simulate_election():
    print("\n🔐 Generating voter keys...")
    voters = []
    for i in range(5):  # Simulate 5 voters
        pub, priv = rsa.newkeys(512)
        voters.append((pub, priv))

    print("\n🗳️ Casting votes...")
    votes = []
    for i, (pub, priv) in enumerate(voters):
        vote = Vote(pub)
        vote.sign(priv)
        votes.append(vote)
        print(f"Voter {i+1} voted for party {vote.get_vote()}")

    print("\n⛓️ Initializing blockchain...")
    blockchain = Blockchain()

    for vote in votes:
        if blockchain.add_vote(vote):
            print("✅ Vote added to blockchain.")
        else:
            print("❌ Invalid vote.")

    print("\n⛏️ Mining new block...")
    blockchain.mine_block()

    print("\n🔍 Validating blockchain...")
    if blockchain.validate_blockchain():
        print("✅ Blockchain is valid.")
    else:
        print("❌ Blockchain is invalid.")

    print("\n📦 Final Blockchain State:")
    blockchain.serialise()

simulate_election()
