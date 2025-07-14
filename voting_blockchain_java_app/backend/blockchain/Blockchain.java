package blockchain;

import java.security.PublicKey;
import java.util.ArrayList; // dynamic list
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Blockchain {

    // Fields
    private final int difficulty = 4;
    private List<Block> chain;
    private Queue<Vote> pendingVotes;
    private Set<PublicKey> remainingVoters;

    // Initialisation
    public Blockchain() {
        this.chain = new ArrayList<>();
        this.pendingVotes = new LinkedList<>();
        this.remainingVoters = new HashSet<>();

        chain.add(new Block("0".repeat(difficulty), new ArrayList<>()));
    }

    // Getters
    public int getDifficulty() {
        return difficulty;
    }

    public List<Block> getChain() {
        return chain;
    }

    public Queue<Vote> getPendingVotes() {
        return pendingVotes;
    }

    public Set<PublicKey> getRemainingVoters() {
        return remainingVoters;
    }

    // Methods
    public boolean createNewBlock(){
        if (pendingVotes.isEmpty()){
            System.out.println("No pending votes");
            return false;
        }

        list<Vote> votesForBlock = new ArrayList<>();
        list<Vote> discardedVotes = new ArrayList<>();
        // Implement vote tracking

        while(!pendingVotes.isEmpty()){
            Vote vote = pendingVotes.poll(); // removed and returns queue head
            PublicKey voterKey = vote.getVoter();

            if (!remainingVoters.contains(voterKey)) {
                System.out.println("Voter has already voted or has not registered: " + voterKey);
                discardedVotes.add(vote);
            } else{
                votesForBlock.add(vote);
                remainingVoters.remove(voterKey);
            }
        }

        if (votesForBlock.isEmpty()) {
            System.out.println("No valid votes to add to block.");
            return false;
        }

        Block lastBlock = getLastBlock();
        Block newBlock = new Block(lastBlock.getHash(), votesForBlock);
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);

        System.out.println("Block successfully mined.")

        return true;

    }

    private Block getLastBlock(){
        // Not useful to be public
        return chain.get(chain.size()-1);
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
                if (!block.isValid()) {
                    System.out.println("Blockchain verification failed: Invalid block");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Block verification failed: " + e.getMessage());
        }
    }


}