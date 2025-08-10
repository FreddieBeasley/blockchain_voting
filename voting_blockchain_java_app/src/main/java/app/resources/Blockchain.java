package app.resources;

import app.LocalNode;
import app.resources.blockchain.resources.Block;
import app.resources.blockchain.resources.Vote;
import app.resources.exceptions.InvalidException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*; // List, ArrayList, Set, HashSet, Queue

public class Blockchain {

    // Fields
    private final LocalNode localNode;

    private final int difficulty;
    private final List<Block> chain;
    private final Queue<Vote> pendingVotes;
    private final Set<String> remainingVoters;

    private final Logger logger = LoggerFactory.getLogger(Blockchain.class);

    // Load new blockchain ( without possible voters )
    public Blockchain(LocalNode localNode) {
        this.localNode = localNode;

        this.difficulty = 4;
        this.chain = new ArrayList<>();
        createGenesisBlock();
        this.remainingVoters = new HashSet<>();
        this.pendingVotes = new LinkedList<>();

    }

    // Load new blockchain ( with possible voters )
    public Blockchain(LocalNode localNode, Set<String> remainingVoters){
        this.localNode = localNode;

        this.difficulty = 4;
        this.chain = new ArrayList<>();
        createGenesisBlock();
        this.remainingVoters = remainingVoters;
        this.pendingVotes = new LinkedList<>();
    }

    // Load existing blockchain
    public Blockchain(LocalNode localNode, int difficulty, List<Block> chain, Set<String> remainingVoters, Queue<Vote> pendingVotes) {
        this.localNode = localNode;

        this.difficulty = difficulty;
        this.chain = chain;
        this.remainingVoters = remainingVoters;
        this.pendingVotes = pendingVotes;
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

    public void createGenesisBlock() {
        List<Vote>  noVotes = new ArrayList<>();
        Block newBlock = new Block("0".repeat(difficulty), noVotes);
        try {
            newBlock.mineBlock(difficulty);
        } catch (InvalidException e) {
            throw new RuntimeException();
        }
        chain.add(newBlock);
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
            } catch(InvalidException e){
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
        }  catch (InvalidException e) {
            logger.warn("Unable to mine block: " + e.getMessage());
            return;
        }

        logger.info("Block successfully mined");

        // Add
        chain.add(newBlock);
        distributeNewBlock(newBlock);

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

    public void isValid() throws InvalidException {
        // Verify Hash Chain
        for (int i = 1; i < chain.size(); i++) {
            String previousBlock_hash = chain.get(i - 1).getHash();
            String currentBlock_previousHash = chain.get(i).getPreviousHash();

            if (!currentBlock_previousHash.equals(previousBlock_hash)) {
                throw new InvalidException("chain broken between block " + i + " and " + i+1);
            }
        }

        // Verify Blocks
        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            try {
                block.isValid(difficulty);
            } catch (InvalidException e) {
                throw new InvalidException("Invalid block at index " + i + " with hash: "   + block.getHash(), e);
            }
        }
    }


    // Pending Votes
    public Queue<Vote> getPendingVotes() {
        return pendingVotes;
    }

    // Receiving
    public void handleNewVote(Vote newVote) {
        logger.info("New vote received");
        logger.info("New vote added to pending votes");
        pendingVotes.add(newVote);
    }

    public void handleNewVoter(String voter) {
        logger.info("New voter received");
        logger.info("New voter added to remaining voters");
        remainingVoters.add(voter);
    }

    public void handleNewBlock(Block newBlock) {
        logger.info("New block received");

        // Checks that the block is valid ( correct hash and proof of work )
        try {
            newBlock.isValid(difficulty);
        } catch (InvalidException e) {
            logger.info("New block discarded: " + e.getMessage());
            return;
        }

        // Checks that the block will not make the chain invalid on adding
        if (!(newBlock.getPreviousHash().equals(getLastBlock().getHash()))) {
            logger.info("New block discarded: block.previousHash does not match chain");
            return;
        }

        // Checks that voters have not already voted and prevents them from voters again in the future
        for (Vote vote: newBlock.getVotes()) {
            removeVoter(vote.getVoter());
        }
        /*
        Here you could think that we should check each vote in the block against remaining voters so that we don't allow a voter to double vote.
        However, we already know that:
            1. the block has a valid hash
            2. the blocks previous hash matched our latest blocks hash
            Because each hash in a blockchain is dependent on the previous hash, there is no need to check against remaining voters since the blockchain that this block came from must be identical to this blockchain to provide a valid previous hash

        However, although we do not need to check that these voter have not already voted, we do need to remove them from remaining voters in order to prevent future double voting
         */
        logger.info("New block accepted");
        chain.add(newBlock);
        distributeNewBlock(newBlock);
    }

    // Sending
    public void distributeNewBlock(Block newBlock) {
        logger.info("Distributing block " + newBlock.getHash());
        localNode.handleNewBlock(newBlock);
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