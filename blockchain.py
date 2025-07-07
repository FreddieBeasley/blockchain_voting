#imports
import helper_functions
import json
import rsa
from hashlib import sha256
from time import time
from collections import deque

class Vote:
     def __init__(self, voter_public_key):
          self.__voter = voter_public_key
          self.__vote = self.assign_vote() 
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

     def serialise(self):
          return {
               "voter": self.get_voter(),
               "vote": str(self.get_vote()),
               "signature": self.__signature.hex() if self.__signature is not None else None
          }
     
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
               "all_votes": self.get_vote_count(), #my want to remove
               "timestamp": self.__timestamp,
               "nonce" : self.__nonce,
               "previous_hash": self.__previous_hash,
               "hash": self.__hash
          }

class Blockchain:
     def __init__(self):
          self.__difficulty = 4
          self.__chain = [Block(votes=[],difficulty=self.__difficulty)] #of Blocks
          self.__pending_votes = deque()
          self.__voters = set() #set of public keys

     # Temporary Methods
     def get_voters(self):
          return self.__voters
     
     def add_voters(self, voter):
          self.__voters.add(voter)
     
     # Getter methods
     def get_chain(self):
          return self.__chain
     
     def get_difficulty(self):
          return self.__difficulty
     
     # Special Methods
     def add_vote(self, vote:Vote):

          if not vote.validate_vote():
               return False

          self.__pending_votes.append(vote)

          return True
     
     def mine_block(self):
          if len(self.__pending_votes) == 0:
               print("No pending votes")
               return False

          votes_to_add = set()
          votes_to_ignore = set()
          votes_tracking = {1:0, 2:0, 3:0, 4:0, 5:0}

          for vote in self.__pending_votes:
               if vote.get_voter() not in self.__voters:
                    print(f"{vote.get_voter()}: Voter has already voted or has not registered to vote")
                    votes_to_ignore.add(vote)
               
               else:
                    votes_to_add.add(vote)
                    self.__voters.remove(vote.get_voter())
                    votes_tracking[vote.get_vote()] += 1

          self.__pending_votes.clear()

          if len(votes_to_add) == 0:
               print('No acceptable votes to add')
               return False
          
          #issue
          vote_count = helper_functions.dictionary_addition(self.get_chain()[-1].get_vote_count(), votes_tracking)

          new_block = Block(votes=list(votes_to_add), difficulty=self.get_difficulty(), previous_hash=self.__chain[-1].get_hash(), vote_count=vote_count)
          
          self.__chain.append(new_block)
          print(f"Block successfully mined - Excluded: {[vote.get_voter() for vote in votes_to_ignore]}")
          return True

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
                    if helper_functions.dictionary_addition(previous_block.get_vote_count(), votes_from_block) != current_block.get_vote_count():
                         print(f"Invalid blockchain: votes are not integral between block {i-1} and {i}")
                         return False
                    
               if not current_block.validate_block():
                    return False
          
          return True

     def serialise(self):
          for block in self.__chain:
               print(block.serialise())



def simulate_election():   
    print("\n⛓️ Initializing blockchain...")
    blockchain = Blockchain()

    print("\n🔐 Generating voter keys...")
    voters = []
    for i in range(5):  # Simulate 5 voters
        pub, priv = rsa.newkeys(512)
        voters.append((pub, priv))
        blockchain.add_voters(pub.save_pkcs1().decode('utf-8'))

    print("\n🗳️ Casting votes...")
    votes = []
    for i, (pub, priv) in enumerate(voters):
        vote = Vote(pub)
        vote.sign(priv)
        votes.append(vote)
        print(f"Voter {i+1} voted for party {vote.get_vote()}")

 

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

    print(blockchain.get_chain()[-1].get_vote_count())

simulate_election()
