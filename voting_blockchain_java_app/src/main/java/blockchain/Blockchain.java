package blockchain;

// JSON formatting
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*; // List, ArrayList, Set, HashSet, Queue

// Local Packages
import util.CryptographyUtils;
import util.FileHandlingUtils;
import util.ParserUtils;



public class Blockchain{

    // Fields
    private int difficulty;
    private final List<Block> chain;
    private final PendingVotes pendingVotes;
    private final Set<String> remainingVoters;
    private final File persistentStorage = new File("data/blockchain.json");
    private final File registeredVoters = new File("data/registeredVoters.json");

    private static final Logger log = LoggerFactory.getLogger(Blockchain.class);



    // Initialisation
    public Blockchain() {
            this.chain = new ArrayList<>();
            this.remainingVoters = new HashSet<>();

            this.pendingVotes = new PendingVotes();
            this.difficulty = 4;

            load();
    }

    // Used for tempBlockchain when loading in from persistent storage
    public Blockchain(List<Block> chain, int difficulty,  Set<String> remainingVoters) {
        this.chain = chain;
        this.remainingVoters = remainingVoters;
        this.pendingVotes = new PendingVotes();
        this.difficulty = difficulty;
    }

    private void persist(){
        try {
           JSONObject persistentJSON = ParserUtils.BlockchainToJSON(this);
           FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(), persistentJSON);

        } catch (Exception e) {
            System.out.println("Persisting Blockchain failed: " + e.getMessage());
        }

    }
    private void load(){
        JSONObject persistentJSON = (JSONObject) FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());
        if (persistentJSON == null){
            log.info("No content to load from");
        }

        else { //Blockchain has been loaded
            try {
                Blockchain tempBlockchain = ParserUtils.JSONToBlockchain(persistentJSON)
            } catch (JSONException je Exception e)
        }

        try{

          Blockchain tempBlockchain = ParserUtils.JSONToBlockchain(persistentJSON);

          if (tempBlockchain.isValid()){
              difficulty = tempBlockchain.getDifficulty();

              chain.clear();
              chain.addAll(tempBlockchain.getChain());

              remainingVoters.clear();
              remainingVoters.addAll(tempBlockchain.getRemainingVoters());
          } else{
              throw new Exception("Blockchain could not be loaded");
          }

        } catch(Exception e){
            System.out.println("Blockchain: " + e.getMessage());
            System.out.println("Blockchain: New blockchain initialised with: \n \t Difficulty: 4 \n \t RemainingVoters: all registered voters\n \t Block: genesis block");


            chain.add(new Block("0".repeat(difficulty), new ArrayList<>()));
            try {
                JSONArray JSONRemainingVoters = (JSONArray) FileHandlingUtils.readFromJSONFile(registeredVoters.getPath());

                if (JSONRemainingVoters == null) {
                    throw new Exception("No valid content to load from");
                }

                remainingVoters.addAll(ParserUtils.JSONArrayToList(JSONRemainingVoters));

            } catch (Exception ex){
                System.out.println("Blockchain ( Remaining Voters ) : " + ex.getMessage());
                System.out.println("Blockchain created with no registered voters");
            }
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

        difficulty = blockchain.getDifficulty();

        chain.clear();
        chain.addAll(blockchain.getChain());

        remainingVoters.clear();
        remainingVoters.addAll(blockchain.getRemainingVoters());

        // PendingVotes will handle itself when a new block is created

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

    public Set<String> getRemainingVoters() {
        return remainingVoters;
    }

    public Queue<Vote> getPendingVotes(){
        return pendingVotes.getPendingVotes();
    }

    public int getLength(){
        return chain.size();
    }

    public Block getLastBlock(){
        if (chain.isEmpty()){
            return null;
        }
        return chain.get(chain.size()-1);
    }

    // Methods
    private boolean removeVoter(String voter){
        if (remainingVoters.remove(voter)){
            persist();
            return true;
        }
        return false;
    }

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
            String voter = CryptographyUtils.publicKeyToString(vote.getVoter());

            if (!vote.isValid()) {
                System.out.println("Invalid vote");
                discardedVotes.add(vote);
            } else if (removeVoter(voter)) {
                votesForBlock.add(vote);
            } else{
                System.out.println("Voter has already voted or has not registered: " + voter);
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
        if (discardedVotes.isEmpty()){
            System.out.println("Block successfully mined");
        } else {
            System.out.println("Block successfully mined - invalid votes discarded from: ");
            for (Vote vote : discardedVotes) {
                System.out.println(vote.getVoter());
            }
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