package blockchain;

import java.security.PublicKey;
import java.util.ArrayList; // dynamic list
import java.util.List;

public class Blockchain{

    // Fields
    private final int difficulty = 4;
    private final List<Block> chain;
    private final PendingVotes pendingVotes;
    private final RemainingVoters remainingVoters;
    // Initialisation
    public Blockchain() {
        this.chain = new ArrayList<>();
        this.pendingVotes = new PendingVotes();
        this.remainingVoters = new RemainingVoters();

        chain.add(new Block("0".repeat(difficulty), new ArrayList<>()));
    }

    // Getters - may not be needed
    public int getDifficulty() {
        return difficulty;
    }

    public List<Block> getChain() {
        return chain;
    }

    // Methods
    public void addNewVote(Vote newVote) {
        pendingVotes.addVote(newVote);
        // make persistent
    }

    public boolean createNewBlock(){
        if (pendingVotes.isEmpty()){
            System.out.println("No pending votes");
            return false;
        }

        // Segregate Valid & Invalid votes
        List<Vote> votesForBlock = new ArrayList<>();
        List<Vote> discardedVotes = new ArrayList<>();

        // Implement vote tracking

        while(!pendingVotes.isEmpty()){
            Vote vote = pendingVotes.pollVote();
            PublicKey voterKey = vote.getVoter();

            if (!vote.isValid()) {
                System.out.println("Invalid vote");
                discardedVotes.add(vote);
            } else if (remainingVoters.removeVoter(voterKey)) {
                votesForBlock.add(vote);
            } else{
                System.out.println("Voter has already voted or has not registered: " + voterKey);
                discardedVotes.add(vote);
            }
        }

        if (votesForBlock.isEmpty()) {
            System.out.println("No valid votes to add to block.");
            return false;
        }

        // Create
        Block lastBlock = getLastBlock();
        Block newBlock = new Block(lastBlock.getHash(), votesForBlock);

        // Mine
        newBlock.mineBlock(difficulty);

        // Add
        chain.add(newBlock);

        // Make Persistent

        // Log
        System.out.println("Block successfully mined - invalid votes discarded from: ");
        for (Vote vote : discardedVotes){
            System.out.println(vote.getVoter());
        }

        return true;

    }

    public Block getLastBlock(){
        // Not useful to be public
        return chain.getLast();
    }

    public Boolean isValid() {
        try {
            // Verify Hash Chain
            for (int i = 1; i < chain.size(); i++) {
                String previousBlock_hash = chain.get(i - 1).getHash();
                String currentBlock_previousHash = chain.get(i).getPreviousHash();

                if (!currentBlock_previousHash.equals(previousBlock_hash)) {
                    System.out.println("Blockchain verification failed: chain broken between block " + i + " and " + (i + 1));
                    return false;
                }
            }

            // Verify Blocks
            for (Block block : chain) {
                if (!block.isValid(difficulty)) {
                    System.out.println("Blockchain verification failed: Invalid block with hash: " + block.getHash());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Block verification failed: " + e.getMessage());
            return false;
        }
    }
}