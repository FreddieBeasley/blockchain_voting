package blockchain;

import util.FileHandlingUtils;

import org.json.JSONArray;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.security.PublicKey;

public class RemainingVoters {
    // Fields
    private Set<PublicKey> remainingVoters = new HashSet<>();
    private final File persistentStorage = new File("data/remaining_voters.json");

    // Initialisation
    public RemainingVoters() {
        load();
    }

    public RemainingVoters(Set<PublicKey> remainingVoters) {
        this.remainingVoters = remainingVoters;
    }

    /*
    Later Implementation
    should be able to add name to this list if they validate using id
    this may occur in a different program before the blockchain/voting is live
     */

    // Getter Method
    public Set<PublicKey> getVoters(){
        return remainingVoters;
    }


    // Persistent Methods
    public void persist(){
        JSONArray jsonArray = new JSONArray();
        for (PublicKey remainingVoter : remainingVoters){
            jsonArray.put(remainingVoter);
        }
        FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(), jsonArray);
    }

    public void load(){
        try {
            JSONArray jsonArray = (JSONArray) FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());
            assert jsonArray != null: "No content to load from";
            for (Object tempObject : jsonArray) {
                remainingVoters.add((PublicKey) tempObject);
            }
        } catch (AssertionError e){
            System.out.println(e.getMessage());
        }
    }

    // Standard Methods
    public boolean removeVoter(PublicKey publicKey){
        if (remainingVoters.remove(publicKey)){
            persist();
            return true;
        }
        return false;
    }



}