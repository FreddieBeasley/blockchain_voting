package app.resources;

import app.resources.blockchain.resources.Block;
import app.resources.blockchain.resources.Vote;
import app.resources.exceptions.InvalidBlockException;
import app.resources.exceptions.InvalidVoteException;
import app.resources.exceptions.InvalidBlockchainException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*; // List, ArrayList, Set, HashSet, Queue

public class Blockchain {

    // Fields
    private final int difficulty;
    private final List<Block> chain;
    private final Queue<Vote> pendingVotes;
    private final Set<String> remainingVoters;

    private final Logger logger = LoggerFactory.getLogger(Blockchain.class);

    // Load new blockchain ( without possible voters )
    public Blockchain() {
        this.difficulty = 4;
        this.chain = new ArrayList<>();
        this.chain.add(createGenesisBlock());
        this.remainingVoters = new HashSet<>();
        this.pendingVotes = new LinkedList<>();

    }

    // Load new blockchain ( with possible voters )
    public Blockchain(Set<String> remainingVoters){
        this.difficulty = 4;
        this.chain = new ArrayList<>();
        this.chain.add(createGenesisBlock());
        this.remainingVoters = remainingVoters;
        this.pendingVotes = new LinkedList<>();
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

    public Block createGenesisBlock(){
        List<Vote>  noVotes = new ArrayList<>();
        return new Block("0".repeat(difficulty), noVotes);
    }

    public void createNewBlock() {
        logger.info("Attempting to create new block");

        if (pendingVotes.isEmpty()) {
            logger.info("No pending votes");
            return;
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
                continue;
            }

            if (removeVoter(voter)) {
                votesForBlock.add(vote);
            } else {
                logger.warn("Voter {} not found", voter);
                discardedVotes.add(vote.serialise());
            }
        }

        if (votesForBlock.isEmpty()) {
            logger.info("No pending votes");
            logDiscardedVotes(discardedVotes);
            return;
        }

        // Create
        Block lastBlock = getLastBlock();
        Block newBlock = new Block(lastBlock.getHash(), votesForBlock);

        // Mine
        try {
            newBlock.mineBlock(difficulty);
        }  catch (InvalidBlockException e) {
            logger.warn(e.getMessage());
            return;
        }

        logger.info("Block successfully mined");

        // Add
        chain.add(newBlock);

        // Logging
        logAcceptedVotes(votesForBlock);
        logDiscardedVotes(discardedVotes);
    }

    // CreateNewBlock() helper methods
    private void logDiscardedVotes(List<String> discardedVotes) {
        if (!discardedVotes.isEmpty()) {
            StringBuilder displayString = new StringBuilder("Block discarded votes from:\n");
            for (String voter : discardedVotes) {
                displayString.append("\t")
                        .append(voter)
                        .append("\n");
            }
            logger.warn(displayString.toString());
        }
    }

    private void logAcceptedVotes(List<Vote> votesForBlock) {
        StringBuilder displayString = new StringBuilder("Block added to blockchain with votes from:\n");
        for (Vote vote: votesForBlock) {
            displayString.append("\t")
                    .append(vote.getVoter())
                    .append("\n");
        }
        logger.info(displayString.toString());
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
        return remainingVoters.remove(voter);
    }

    /*
    This could all be seperated into different classes:
        1 for chain ( or probably keep chain logic here )
        1 for remaining voters
        1 for Pending Votes
     */

}