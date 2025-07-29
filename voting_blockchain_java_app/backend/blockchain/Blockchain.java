package blockchain;

import org.json.JSONObject;

import util.FileHandlingUtils;
import util.ParserUtils;

import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList; // dynamic list
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Blockchain{

    // Fields
    private int difficulty;
    private final List<Block> chain;
    private final PendingVotes pendingVotes;
    private RemainingVoters remainingVoters;
    private final File persistentStorage = new File("data/blockchain.json");

    // Initialisation
    public Blockchain() {
            this.chain = new ArrayList<>();
            this.pendingVotes = new PendingVotes();
            this.remainingVoters = new RemainingVoters();
            this.difficulty = 4;

            load();
    }

    // Used for tempBlockchain when loading in from persistent storage
    public Blockchain(List<Block> chain, int difficulty) {
        this.chain = chain;
        this.difficulty = difficulty;
        this.pendingVotes = new PendingVotes();
        this.remainingVoters = new RemainingVoters();
    }

    private void persist(){
        try {
            JSONObject JSONBlockchain = ParserUtils.BlockchainToJSON(this);
            FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(),JSONBlockchain);

        } catch (Exception e) {
            System.out.println("Persisting Blockchain failed: " + e.getMessage());
        }

    }
    private void load(){
        try{
            JSONObject JSONBlockchain = (JSONObject) FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());
            assert JSONBlockchain != null;

            Blockchain tempBlockchain = ParserUtils.JSONToBlockchain(JSONBlockchain);

            if (!tempBlockchain.isValid()){
                throw new Exception("Invalid persisted Blockchain not adopted");
            }
            // load chain
            chain.clear();
            chain.addAll(tempBlockchain.getChain());
            difficulty = tempBlockchain.getDifficulty();

        } catch(Exception e){
            System.out.println(e.getMessage());
            chain.add(new Block("0".repeat(difficulty), new ArrayList<>()));
        }
    }

    public boolean attemptConsensus(Blockchain blockchain){
        // New blockchain must be longer
        if (blockchain.getLength() <= this.getLength()){
            System.out.println("Consensus failed: new blockchain is shorter than current blockchain");
            return false;
        }
        // New blockchain must have same or greater difficulty
        else if (blockchain.getDifficulty() < difficulty){
            System.out.println("Consensus failed: difficulty is lower than current blockchain");
            return false;
        }
        // New blockchain must be valid
        else if (!blockchain.isValid()){
            System.out.println("Consensus failed: new blockchain is not valid");
            return false;
        }
        chain.clear();
        chain.addAll(blockchain.getChain());
        difficulty = blockchain.getDifficulty();
        remainingVoters = new RemainingVoters(blockchain.getRemainingVoters());
        // PendingVotes does not need to updates as repeated votes will automatically be discarded on block creation

        persist();
        return true;
    }

    // Getters - may not be needed
    public int getDifficulty() {
        return difficulty;
    }

    public List<Block> getChain() {
        return chain;
    }

    public Set<PublicKey> getRemainingVoters() {
        return remainingVoters.getVoters();
    }

    public Queue<Vote> getPendingVotes(){
        return pendingVotes.getPendingVotes();
    }

    public int getLength(){
        return chain.size();
    }

    public Block getLastBlock(){
        return chain.getLast();
    }

    // Methods
    public void addNewVote(Vote newVote) {
        pendingVotes.addVote(newVote);
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

        // Persist
        persist();

        // Log
        System.out.println("Block successfully mined - invalid votes discarded from: ");
        for (Vote vote : discardedVotes){
            System.out.println(vote.getVoter());
        }

        return true;

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