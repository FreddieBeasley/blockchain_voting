package blockchain;

import exceptions.InvalidBlockException;
import exceptions.InvalidVoteException;
import exceptions.InvalidBlockchainException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*; // List, ArrayList, Set, HashSet, Queue

public class Blockchain {

    // Fields
    private int difficulty;
    private final List<Block> chain;
    private final Queue<Vote> pendingVotes;
    private final Set<String> remainingVoters;

    private final Logger logger = LoggerFactory.getLogger(Blockchain.class);

    // Load new blockchain ( without possible voters )
    public Blockchain() {
        this.chain = new ArrayList<>();
        this.remainingVoters = new HashSet<>();
        this.pendingVotes = new LinkedList<>();
        this.difficulty = 4;

    }

    // Load new blockchain ( with possible voters )
    public Blockchain(Set<String> remainingVoters){
        this.chain = new ArrayList<>();
        this.remainingVoters = remainingVoters;
        this.pendingVotes = new LinkedList<>();
        this.difficulty = 4;
    }

    // Load existing blockchain
    public Blockchain(List<Block> chain, Set<String> remainingVoters, Queue<Vote> pendingVotes, int difficulty) {
        this.chain = chain;
        this.remainingVoters = remainingVoters;
        this.pendingVotes = pendingVotes;
        this.difficulty = difficulty;
    }


    // Difficulty
    public int getDifficulty() {
        return difficulty;
    }

    // Chain
    public List<Block> getChain() {
        return chain;
    }

    public int getLength() {
        return chain.size();
    }

    public Block getLastBlock() {
        if (chain.isEmpty()) {
            return null;
        }
        return chain.get(chain.size() - 1);
    }

    public Block getBlock(int index){
        return chain.get(index);
    }

    public boolean createNewBlock() {
        if (pendingVotes.isEmpty()) {
            System.out.println("No pending votes");
            return false;
        }

        // Segregate Valid & Invalid votes
        List<Vote> votesForBlock = new ArrayList<>();
        List<String> discardedVotes = new ArrayList<>();

        // Implement vote tracking

        while (!pendingVotes.isEmpty()) {
            Vote vote = pendingVotes.poll();
            String voter = vote.getVoter();

            try{
                vote.isValid();
            } catch(InvalidVoteException e){
                logger.warn("Invalid vote from {}", voter, e);
                discardedVotes.add(vote.serialise());
            }

            if (removeVoter(voter)) {
                votesForBlock.add(vote);
            } else {
                logger.warn("Voter {} not found", voter);
                discardedVotes.add(vote.serialise());
            }
        }

        if (votesForBlock.isEmpty()) {
            logger.info("No votes to add to blockchain");
            return false;
        }

        // Create
        Block lastBlock = getLastBlock();
        Block newBlock = new Block(lastBlock.getHash(), votesForBlock);

        // Mine
        newBlock.mineBlock(difficulty);

        // Add
        chain.add(newBlock);

        // Log
        if (discardedVotes.isEmpty()) {
            logger.info("Block successfully created");
        } else {
            logger.info("Block successfully created, votes discarded from: {}", discardedVotes);
        }

        return true;

    }

    public void isValid() throws InvalidBlockchainException {
        // Verify Hash Chain
        for (int i = 1; i < chain.size(); i++) {
            String previousBlock_hash = chain.get(i - 1).getHash();
            String currentBlock_previousHash = chain.get(i).getPreviousHash();

            if (!currentBlock_previousHash.equals(previousBlock_hash)) {
                throw new InvalidBlockchainException("chain broken between block " + i + " and " + i+1);
            }
        }

        // Verify Blocks
        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            try {
                block.isValid(difficulty);
            } catch (InvalidBlockException e) {
                throw new InvalidBlockchainException("Invalid block at index " + i + " with hash: "   + block.getHash(), e);
            }
        }

        for (Block block : chain) {
            try {
                block.isValid(difficulty);
            } catch (InvalidBlockException e) {
                throw new InvalidBlockchainException("Invalid block with hash " + block.getHash(), e);
            }
        }
    }

    // Pending Votes
    public Queue<Vote> getPendingVotes() {
        return pendingVotes;
    }

    public void addNewVote(Vote newVote) {
        pendingVotes.add(newVote);
    }

    // Remaining Voters
    public Set<String> getRemainingVoters() {
        return remainingVoters;
    }

    private boolean removeVoter(String voter) {
        if (remainingVoters.remove(voter)) {
            return true;
        }
        return false;
    }

    /*
    This could all be seperated into different classes:
        1 for chain ( or probably keep chain logic here )
        1 for remaining voters
        1 for Pending Votes
     */

}