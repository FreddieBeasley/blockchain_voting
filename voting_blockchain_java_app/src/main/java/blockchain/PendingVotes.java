/*
Function : stores a Queue containing votes that have been submitted but not added to a block yet

Functionality :
    - Does not need to be encrypted - votes cannot be altered PrivateKey
    - Needs to be persistent - votes should not be lost due to a system failure
 */
package blockchain;

import org.json.JSONArray;
import org.json.JSONObject;
import util.FileHandlingUtils;
import util.ParserUtils;

import java.io.File;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.Queue;

public class PendingVotes {
    // Fields
    private final Queue<Vote> pendingVotes  = new LinkedList<>();
    private final File persistentStorage = new File("src/main/data/pendingVotes.json");

    // Initialisation
    public PendingVotes() {
        load();
    }

    // Getter
    public Queue<Vote> getPendingVotes() {
        return pendingVotes;
    }

    // Persistent Methods
    private void persist(){
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject tempObject = new JSONObject();
            for (Vote vote : pendingVotes) {
                tempObject = ParserUtils.VoteToJSON(vote);
                jsonArray.put(tempObject);
            }
            FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(), jsonArray);
        } catch (Exception e){
            System.out.println("Error while saving PendingVotes");
        }
    }

    private void load(){
        try {
             JSONArray jsonArray = (JSONArray)  FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());

             if (jsonArray == null){
                 throw new Exception("No valid content to load from");
             }

             for (Object tempObject : jsonArray) {
                 JSONObject tempJSONObject = (JSONObject) tempObject;
                 pendingVotes.add(new Vote(tempJSONObject.getString("Voter"),  tempJSONObject.getInt("VoteValue"),  tempJSONObject.getString("Signature")));
             }

        } catch (Exception e) {
            System.out.println("PendingVotes: " + e.getMessage());
            System.out.println("PendingVotes: New empty queue created");

        }
    }

    // Standard Methods
    public boolean isEmpty() {
        return pendingVotes.isEmpty();
    }

    public boolean containsVote(PublicKey voter) {
        for (Vote vote : pendingVotes) {
            if (vote.getVoter().equals(voter)) {
                return true;
            }
        }
        return false;
    }

    public void addVote(Vote vote) {
        pendingVotes.add(vote);
        persist();
    }

    public Vote pollVote() {
       Vote returnVote = pendingVotes.poll();
       persist();
       return returnVote;
    }
}