package blockchain;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import util.CryptographyUtils;
import exceptions.InvalidVoteException;

public class Vote {

    // Fields
    private final String voter;
    private final int voteValue;
    private String signature;

    // Initialisation ( for creating  a vote)
    public Vote(String voter, int voteValue) {
        this.voter = voter;
        this.voteValue = voteValue;
    }

    // Initialisation ( for restoring a vote send throw the network )
    public Vote(String voter, int voteValue, String signature) {
        this.voter = voter;
        this.voteValue = voteValue;
        this.signature = signature;
    }

    // Getters
    public String getVoter() {
        return voter;
    }

    public PublicKey getVoterAsPublicKey(){
        try {
            return CryptographyUtils.stringToPublicKey(voter);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new RuntimeException(e);
        }
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
            String data = voter + voteValue;

            Signature signer = Signature.getInstance("SHA256withRSA"); // Signature object for SHA-256
            signer.initSign(privateKey); // Initialises object for signing ( with given private key )
            signer.update(data.getBytes()); // Updates data to be signed ( with vote data )

            this.signature = Base64.getEncoder().encodeToString(signer.sign());
        } catch(Exception e){
        throw new RuntimeException("Error signing vote:", e);
        }
    }

    public void isValid() throws InvalidVoteException {
        Boolean isValid;

        try {
            String data = voter + voteValue;
            Signature verifier = Signature.getInstance("SHA256withRSA"); // Signature object for SHA-256
            verifier.initVerify(getVoterAsPublicKey()); // Initialises the object for verification ( with expected signers public_key )
            verifier.update(data.getBytes()); // Updates data to be verified ( with expected vote data )
            isValid = verifier.verify(Base64.getDecoder().decode(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InvalidVoteException("Unable to verify vote", e);
        }

        if (!isValid) {
            throw new InvalidVoteException("Invalid Signature");
        }


    }

    public String serialise() {
            return voter + "|||" + voteValue + "|||" + signature;
    }
}