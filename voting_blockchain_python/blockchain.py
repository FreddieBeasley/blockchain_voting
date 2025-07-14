#imports
import json
import rsa
from hashlib import sha256
from time import time
from collections import deque

class Vote:
     def __init__(self, voter_public_key, vote=None):
          self.__voter = voter_public_key
          self.__vote = vote if vote in [1,2,3,4,5] else self.assign_vote() 
          self.__signature = None

     #getters
     def get_voter_public_key(self):
          return self.__voter
     def get_voter(self):
          return self.__voter.save_pkcs1().decode("utf-8")
     
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
          data = f"{self.get_voter()}{self.get_vote()}"
          self.__signature = rsa.sign(data.encode("utf-8"), private_key, "SHA-256")

     def validate_vote(self):
          # Checking for signature
          if not self.__signature:
               print(f"{self.get_voter()} | {self.__vote}: Missing Signature")
               return False
          
          # Verifying signature
          data = f"{self.get_voter()}{self.__vote}"

          try:
               rsa.verify(data.encode(), self.__signature, self.__voter)
               return True
          
          except rsa.VerificationError:
               print(f"Invalid signature for vote by {self.__voter}")
               return False

     def to_dict(self):
          return {
               "voter": self.get_voter(),
               "vote": str(self.get_vote()),
               "signature": self.__signature.hex() if self.__signature is not None else None
          }
     
     @classmethod
     def from_dict(cls, data):
          public_key = rsa.PublicKey.load_pkcs1(data["voter"].encode())
          vote_data = int(data["vote"])

          new_vote = cls(voter_public_key=public_key, vote=vote_data)

          new_vote._Vote__signature = bytes.fromhex(data["signature"]) if data["signature"] else None

          return new_vote
     
class Block:
     def __init__(self, votes:list, difficulty, previous_hash="0000", vote_count=None):

          #Vote Tracking
          self.__votes = votes
          self.__vote_count = vote_count if vote_count is not None else {1:0, 2:0, 3:0, 4:0, 5:0}

          #Hashing & Validation
          self.__difficulty = difficulty
          self.__timestamp = time()
          self.__nonce = 0
          self.__previous_hash = previous_hash
          self.__hash = self.__proof_of_work()


     # Getter methods
     def get_votes(self):
          return self.__votes
     
     def get_vote_count(self):
          return self.__vote_count
     
     def get_timestamp(self):
          return self.__timestamp
     
     def get_previous_hash(self):
          return self.__previous_hash
     
     def get_hash(self):
          return self.__hash
     
     # Setter methods
     def set_miner(self, miner_id:str):
          self.__miner = miner_id

     def __proof_of_work(self):
          hash = self.__calculate_hash()

          while hash[:self.__difficulty] != "0"*self.__difficulty:
               self.__nonce +=1
               hash = self.__calculate_hash()

          return hash
          
     def __calculate_hash(self):
          vote_str = ""

          for vote in self.__votes:
               vote_str += json.dumps(vote.to_dict(), sort_keys=True)

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

     def to_dict(self):
          return {
               "votes": [vote.to_dict() for vote in self.__votes],
               "vote_count": self.get_vote_count(),
               "timestamp": self.__timestamp,
               "nonce" : self.__nonce,
               "previous_hash": self.__previous_hash,
               "hash": self.__hash
          }
     
     @classmethod
     def from_dict(cls, data):

          votes = [Vote.from_dict(vote_data) for vote_data in data["votes"]]
     
          new_block = cls(votes=votes, difficulty=4, previous_hash = data["previous_hash"], vote_count = data["vote_count"]) #may want to store difficulty in to_dict later

          new_block._Block__timestamp = data["timestamp"]
          new_block._Block__nonce = data["nonce"]
          new_block._Block__hash = data["hash"]

          return new_block


class Blockchain:

     # Initialisation
     def __init__(self):
          self.__difficulty = 4
          self.__chain = [Block(votes=[],difficulty=self.__difficulty)] #of Blocks
          self.__pending_votes = deque()
          self.__voters = set() #set of public keys
     
     # Basic Static Methods
     @staticmethod
     def normalise_key_str(key_str):
          return "".join(key_str.split())

     @staticmethod
     def generate_keys():
          return rsa.newkeys(512)
     
     @staticmethod
     def stringify_key(key):
          return key.save_pkcs1().decode()
     
     #Voter Management     
     def register_to_voters(self):
          public_key, private_key = Blockchain.generate_keys()
          normalised_key = Blockchain.normalise_key_str(Blockchain.stringify_key(public_key))

          if normalised_key not in self.__voters:
               self.__voters.add(normalised_key)
               return private_key
          
          return None

     
     # Basic Getter Methods
     def get_chain(self):
          return self.__chain
     
     def get_difficulty(self):
          return self.__difficulty
     
     def get_voters(self):
          return self.__voters
     
     # Voting & Block Logic
     def cast_vote(self, vote):

          if not vote.validate_vote():
               return False

          self.__pending_votes.append(vote)

          return True
     
     def mine_block(self):
          if len(self.__pending_votes) == 0:
               print("No pending votes")
               return False

          votes_to_add = []
          votes_to_ignore = []
          votes_tracking = {1:0, 2:0, 3:0, 4:0, 5:0}

          for vote in self.__pending_votes:
               normalised_key = Blockchain.normalise_key_str(vote.get_voter())
               if normalised_key not in self.__voters:
                    print(f"{normalised_key}: Voter has already voted or has not registered to vote")
                    votes_to_ignore.append(vote)
               
               else:
                    votes_to_add.append(vote)
                    self.__voters.remove(normalised_key)
                    votes_tracking[vote.get_vote()] += 1

          self.__pending_votes.clear()

          if len(votes_to_add) == 0:
               print('No acceptable votes to add')
               return False
          
          vote_count = Blockchain.dictionary_addition(self.get_chain()[-1].get_vote_count(), votes_tracking)

          new_block = Block(votes=votes_to_add, difficulty=self.get_difficulty(), previous_hash=self.__chain[-1].get_hash(), vote_count=vote_count)
          
          self.__chain.append(new_block)
          print(f"Block successfully mined - Excluded: {[vote.get_voter() for vote in votes_to_ignore]}")
          return True
     
     @staticmethod
     def dictionary_addition(dict1:dict, dict2:dict):
          new_dict = {}
          new_keys = set(dict1.keys()) | set(dict2.keys())

          for new_key in new_keys:
               new_dict[new_key] = 0

               for dict in [dict1, dict2]:
                    if new_key in dict:
                         try:
                              new_dict[new_key] += dict[new_key]
                         except:
                              raise ValueError('dictionaries must contains the same data types')
          
          return new_dict

     # Validation
     def validate_blockchain(self):
          for i in range(1, len(self.__chain)):
               if len(self.__chain) > 1:
                    previous_block = self.__chain[i - 1]
                    current_block = self.__chain[i]
                    
                    # Validating Hash
                    if previous_block.get_hash() != current_block.get_previous_hash():
                         print(f"Invalid blockchain: chain is broken between block {i-1} and {i}")
                         return False
                    
                    # Validating Count
                    votes_from_block = {1:0, 2:0, 3:0, 4:0, 5:0}
                    for vote in current_block.get_votes():
                         votes_from_block[vote.get_vote()] += 1
                    if Blockchain.dictionary_addition(previous_block.get_vote_count(), votes_from_block) != current_block.get_vote_count():
                         print(f"Invalid blockchain: votes are not integral between block {i-1} and {i}")
                         return False
                    
               if not current_block.validate_block():
                    return False
          
          return True
     
     # Ouput & Display
     def display_election_result(self):
          result = self.get_chain()[-1].get_vote_count()

          for party, count in result.items():
               print(f"Party {party}: {count} vote(s)")

     # JSON Handing
     def to_dict(self):
          return {
               "chain": [block.to_dict() for block in self.get_chain()],
               "voters": list(self.get_voters()),
               "pending_votes": [vote.to_dict() for vote in self.__pending_votes]
          }
     
     @classmethod
     def from_dict(cls, data):
          
          chain = [Block.from_dict(block_data) for block_data in data["chain"]]
     

          new_blockchain = cls()
          new_blockchain._Blockchain__chain = chain
          new_blockchain._Blockchain__voters = set(data["voters"])
     
          new_blockchain._Blockchain__pending_votes = deque(Vote.from_dict(vote_data) for 
          vote_data in data["pending_votes"])

          return new_blockchain

     def serialise(self):
          print(json.dumps(self.to_dict(), indent=4))


def save_blockchain(blockchain, filename):
     file = open(filename, "w")
     json.dump(blockchain.to_dict(), file)
     file.close()

def load_blockchain(filename):
     file = open(filename, "r")
     data = json.load(file)
     file.close()

     return Blockchain.from_dict(data)



def main():
     #For debugging for now but runs through what the webapp should do when it is made

     #allowing for voters to register
     """
     This will take a username (BEASFB) and there password (Beasley20!9/2o). Given that it is right they will be given a private and public key. The public key will be stored in the blockchain
     """