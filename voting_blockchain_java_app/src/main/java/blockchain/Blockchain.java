package blockchain;

// JSON formatting
import exceptions.InvalidBlockException;
import exceptions.InvalidVoteException;
import exceptions.MalformedJSONBlockchainException;
import org.json.JSONArray;
import org.json.JSONObject;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*; // List, ArrayList, Set, HashSet, Queue

// Local Packages

//import util.CryptographyUtils;
import util.FileHandlingUtils;
import util.ParserUtils;
import exceptions.InvalidBlockchainException;



public class Blockchain {

    // Fields
    private int difficulty;
    private final List<Block> chain;
    private final PendingVotes pendingVotes;
    private final Set<String> remainingVoters;
    private final File persistentStorage = new File("src/main/data/blockchain.json");
    private final File registeredVoters = new File("src/main/data/registeredVoters.json");

    private final Logger logger = LoggerFactory.getLogger(Blockchain.class);

    // Initialisation
    public Blockchain() {
        logger.info("Blockchain initialized");

        this.chain = new ArrayList<>();
        this.remainingVoters = new HashSet<>();

        this.pendingVotes = new PendingVotes();
        this.difficulty = 4;

        try {
            load();
        } catch (Exception e) {
            logger.warn("Unable to load blockchain: ", e);
            initialise();
        }
    }


    // Used for tempBlockchain when loading in from persistent storage
    public Blockchain(List<Block> chain, int difficulty, Set<String> remainingVoters) {
        this.chain = chain;
        this.remainingVoters = remainingVoters;
        this.pendingVotes = new PendingVotes();
        this.difficulty = difficulty;
    }

    private void persist() {
        try {
            JSONObject persistentJSON = ParserUtils.BlockchainToJSON(this);
            FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(), persistentJSON);
        } catch (IOException e) {
            logger.error("Error while persisting blockchain to JSON file", e.getMessage());
        }

    }

    private void initialise() {
        difficulty = 4;

        chain.clear();
        Block genesisBlock = new Block("0".repeat(difficulty), new ArrayList<>());
        genesisBlock.mineBlock(difficulty);
        chain.add(genesisBlock);

        try {
            JSONArray JSONRemainingVoters = (JSONArray) FileHandlingUtils.readFromJSONFile(registeredVoters.getPath());

            remainingVoters.addAll(ParserUtils.JSONArrayToList(JSONRemainingVoters));
            logger.info("Blockchain initialised with all registered voters");


        } catch (Exception e) {
            logger.warn("Unable to load registered voters");
            logger.info("Blockchain initialised with no registered voters");
            remainingVoters.clear();
        }

    }

    private void load() throws IOException {
        JSONObject persistentJSON = (JSONObject) FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());

        //System.out.println(persistentJSON.toString(4));

        Blockchain tempBlockchain;

        try {
            tempBlockchain = ParserUtils.JSONToBlockchain(persistentJSON); // Throws MalformedJSONBlockchainException
        } catch (MalformedJSONBlockchainException e) {
            throw new IOException("Malformed blockchain stored", e);
        } catch (NullPointerException e) {
            throw new IOException("No blockchain stored to load from");
        }

        System.out.println(ParserUtils.BlockchainToJSON(tempBlockchain));
        Block suspiciousBlock = tempBlockchain.getBlock(0);
        System.out.println(suspiciousBlock.getPreviousHash() + " " + suspiciousBlock.getVotes() + " " + suspiciousBlock.getNonce() + " " + suspiciousBlock.getHash() + " " + suspiciousBlock.getTimestamp());
        System.out.println(suspiciousBlock.getHash().getClass().getSimpleName() + " " + suspiciousBlock.getVotes().getClass().getSimpleName() + " " + suspiciousBlock.getNonce());
        System.out.println(suspiciousBlock.computeHash());
        try {
            tempBlockchain.isValid();
        } catch (InvalidBlockchainException e) {
            throw new IOException("Invalid blockchain stored", e);
        }

        difficulty = tempBlockchain.getDifficulty();

        chain.clear();
        chain.addAll(tempBlockchain.getChain());

        remainingVoters.clear();
        remainingVoters.addAll(tempBlockchain.getRemainingVoters());

        for (Vote vote : tempBlockchain.getPendingVotes()) {
            pendingVotes.addVote(vote);
        }

        logger.info("Blockchain loaded from persistent storage");
    }

    public boolean attemptConsensus(Blockchain blockchain) {
        // New blockchain must be longer
        if (blockchain.getLength() <= this.getLength()) {
            //log
            return false;
        }

        // New blockchain must have same or greater difficulty
        else if (blockchain.getDifficulty() < difficulty) {
            //log
            return false;
        }

        // New blockchain must be valid
        try {
            blockchain.isValid();
        } catch (InvalidBlockchainException e) {
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

    public Queue<Vote> getPendingVotes() {
        return pendingVotes.getPendingVotes();
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

    // Methods
    private boolean removeVoter(String voter) {
        if (remainingVoters.remove(voter)) {
            persist();
            return true;
        }
        return false;
    }

    public void addNewVote(Vote newVote) {
        pendingVotes.addVote(newVote);
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
            Vote vote = pendingVotes.pollVote();
            String voter = vote.getVoter();

            try{
                vote.isValid();
            } catch(InvalidVoteException e){
                logger.warn("Invalid vote from " + voter, e);
                discardedVotes.add(vote.serialise());
            }

            if (removeVoter(voter)) {
                votesForBlock.add(vote);
            } else {
                logger.warn("Voter " + voter + " not found");
                discardedVotes.add(vote.serialise());
            }
        }

        if (votesForBlock.isEmpty()) {
            logger.info("No votes to add to blocchain");
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
        if (discardedVotes.isEmpty()) {
            logger.info("Block successfully created");
        } else {
            logger.info("Block successfully created, votes discarded from: " + discardedVotes);
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

}