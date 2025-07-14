package blockchain;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import org.json.JSONObject;

public class Vote {

    // Fields
    private publicKey voter;
    private int voteValue;
    private String signature;

    // Initialisation ( for creating  a vote)
    public Vote(PublicKey voter, int voteValue) {
        this.voter = voter;
        this.voteValue = voteValue;
    }

    // Initialisation ( for restoring a vote send throw the network )
    public Vote(PublicKey voter, int voteValue, String signature) {
        this.voter = voter;
        this.voteValue = voteValue;
        this.signature = signature;
    }

    // Getters
    public publicKey getVoter() {
        return voter;
    }

    public String getVoterAsString(){
        return Base64.getEncoder().encodeToString(voter.getEncoded());
    }

    public int  getVoteValue() {
        return voteValue;
    }

    public String getSignature() {
        return signature;
    }

    // Methods
    public void signVote(PrivateKey privateKey) {
        try{
            String data = getVoterAsString() + voteValue;

            Signature signer = Signature.getInstance("SHA256withRSA"); // Signature object for SHA-256
            signer.initSign(privateKey); // Initialises object for signing ( with given private key )
            signer.update(data.getBytes()); // Updates data to be signed ( with vote data )

            this.signature = Base64.getEncoder().encodeToString(signer.sign());
        } catch(Exception e){
        throw new RuntimeException("Error signing vote:", e);
        }
    }

    public boolean isValid() {
        try {
            String data = getVoterAsString() + voteValue;

            Signature verifier = Signature.getInstance("SHA256withRSA"); // Signature object for SHA-256
            verifier.initVerify(voter); // Initialises the object for verification ( with expected signers public_key )
            verifier.update(data.getBytes()); // Updates data to be verified ( with expected vote data )

            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            System.err.println("Vote verification failed: " + e.getMessage());
            return false;
        }
    }

    public JSONObject jsonify(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("voter", voter);
        jsonObject.put("vote", voteValue);
        if (signature != null) {
            jsonObject.put("signature", signature);
        }

        return jsonObject;

    }

    public String serialise(){
        return jsonify().toString();
    }
}