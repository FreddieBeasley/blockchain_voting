package app.resources.blockchain.resources;

import app.resources.util.Cryptography;
import app.resources.exceptions.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Block {

    // Fields
    private final List<Vote> votes;
    private final String previousHash;
    private String hash;
    private final long timestamp;
    private int nonce;

    // Initialisation ( for creating  a block)
    public Block (String previousHash, List<Vote> votes){
        this.votes = votes;
        this.previousHash = previousHash;
        this.timestamp = System.currentTimeMillis();
        this.nonce = 0;
        this.hash = computeHash();
    }

    // Initialisation ( for constructing a block from a peer or persistent file )
    public Block(String previousHash, List<Vote> votes, String hash, long timestamp, int nonce) {
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

    public List<Vote> getVotes(){
        return votes;
    }

    public long getTimestamp() {return timestamp;}

    public int getNonce() {return nonce;}

    // Methods
    public String computeHash() {
      String data = serialise();
      try {
          return Cryptography.hash(data);
      } catch (NoSuchAlgorithmException e) {
          hash = "";
          throw new IllegalStateException("SHA-256 algorithm is not available");
      }
    }

    public void mineBlock(int difficulty) throws InvalidException {
        String prefix = "0".repeat(difficulty);

        while (!hash.startsWith(prefix)) {
            nonce++;
            hash = computeHash();

            if (hash == null) {
                throw new InvalidException("Hash computation failed");
            }

        }

    }

    public void isValid(int difficulty) throws InvalidException {

        // Verify block hash
        String computedHash = computeHash();
        if (!computedHash.equals(hash)){
            throw new InvalidException("Block :Invalid hash: \n \t Computed Hash:\t" + computedHash + "\n \t Stored Hash:\t" + hash);
        }

        // Verify proof of work
        String prefix = "0".repeat(difficulty);
        if (!hash.startsWith(prefix)) {
            throw new InvalidException("Block: No proof of work: \n \t Hash: " + hash);
        }

        // Verify block votes
        for (Vote vote: votes) {
            try {
                vote.isValid();
            }  catch (InvalidException e) {
                throw new InvalidException("Block: Contains Invalid Vote", e);
            }

        }
    }

    public String serialise() {
        StringBuilder sb = new StringBuilder();
        for (Vote vote: votes){
            sb.append(vote.serialise());
        }
        return sb.toString() + "||||||" + previousHash + "||||||" + timestamp + "||||||" + nonce;
    }

}