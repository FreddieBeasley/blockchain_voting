package blockchain;

import java.util.List;
import java.util.ArrayList;
import java.security.MessageDigest;
import org.json.JSONObject;

public class Block {

    // Fields
    private List<Vote> votes = new ArrayList<>();
    private String previousHash;
    private String hash;
    private long timestamp;
    private int nonce;

    // Initialisation ( for creating  a vote)
    public Block (String previousHash, List<Votes> votes){
        this.votes = votes;
        this.previousHash = previousHash;
        this.hash = calculateHash();
        this.timestamp = System.currentTimeMillis();
        this.nonce = 0;
    }

    // Initialisation ( for constructing a shared vote send throw the network )
    public Block(String previousHash, List<Votes> votes, String hash, long timestamp, int nonce) {
        this.votes = votes;
        this.previousHash = previousHash;
        this.hash = hash;
        this.timestamp = timestamp;
        this.nonce = nonce;
    }

    // Getters
    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public list<Vote> getVotes(){
        return votes;
    }

    public long getTimestamp() {return timestamp;}

    public int getNonce() {return nonce;}

    // Methods
    public String computeHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // Message digest object for SHA-256

            StringBuilder voteData = new StringBuilder();
            for (Vote vote: votes){
                voteData.append(vote.serialise());
            }

            String data = previousHash + timestamp + voteData.toString() + nonce;

            md.update(data.getBytes()); // Passes byte array to the messge digest object
            byte[] digest = md.digest(); // Generates message digest as byte array

            return Base64.getEncoder().encodeToString(digest)

        } catch (Exception e) {
            System.err.println("Error computing hash: " + e.getMessage());
            return null;
        }
    }

    public void mineBlock(int difficulty) {
        String prefix = "0".repeat(difficulty);

        while (!hash.startsWith(prefix)) {
            hash = computeHash();

            if (hash == null) {
                System.err.println("Hash computation failed. Mining aborted.");
            }

            nonce++;
        }

        System.out.println("Block successfully mined.")
    }

    public boolean isValid(int difficulty) {
        try{
            // Verify block hash
            string computedHash = computeHash()
            if (!computedHash.equals(hash)){
                System.out.println("Block verification failed: Invalid block hash")
                return false;
            }

            // Verify proof of work
            String prefix = "0".repeat(difficulty);
            if (!hash.startsWith(prefix)) {
                System.out.println("Block verification failed: No proof of work")
                return false;
            }

            // Verify block votes
            for (Vote vote: votes){
                if (!vote.isValid()) {
                    System.out.println("Block verification failed: Invalid vote")
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Block verification failed: " + e.getMessage());
            return false;
        }
    }

    public JSONObject jsonify(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("hash", hash);
        jsonObject.put("previousHash", previousHash);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("nonce", nonce);

        JSONArray voteArray = new JSONArray();
        for (Vote vote: votes) {
            jsonArray.put(vote.jsonify());
        }

        jsonObject.put("votes", voteArray);

        return jsonObject;
    }

    public String serialise(){
        return jsonify().toString();
    }

}