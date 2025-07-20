package blockchain;

import java.util.HashSet;
import java.util.Set;
import java.security.PublicKey;

public class RemainingVoters {
    // Fields
    private final Set<PublicKey> remainingVoters;

    // Initialisation
    public RemainingVoters() {
        this.remainingVoters = new HashSet<>(); // should load from persistent file
    }

    /*
    Later Implementation
    should be able to add name to this list if they validate using id
    this may occur in a different program before the blockchain/voting is live
     */

    // Methods

    public boolean removeVoter(PublicKey publicKey){
        return remainingVoters.remove(publicKey);
    }







}