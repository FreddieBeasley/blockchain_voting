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

import java.io.File;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.Queue;

public class PendingVotes {
    // Fields
    private final Queue<Vote> pendingVotes  = new LinkedList<>();
    private final File persistentStorage = new File("data/pendingVotes.json");

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
        JSONArray jsonArray = new JSONArray();
        JSONObject tempObject = new JSONObject();
        for (Vote vote : pendingVotes) {
            tempObject.put("Voter",  vote.getVoter());
            tempObject.put("VoteValue",  vote.getVoteValue());
            tempObject.put("Signature",  vote.getSignature());
            jsonArray.put(tempObject);
        }
        FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(), jsonArray);
    }

    private void load(){
        try {
             JSONArray jsonArray = (JSONArray)  FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());
             assert jsonArray != null: "No content to load from";
             for (Object tempObject : jsonArray) {
                 JSONObject tempJSONObject = (JSONObject) tempObject;
                 pendingVotes.add(new Vote( (PublicKey)  tempJSONObject.get("Voter"),  tempJSONObject.getInt("VoteValue"),  tempJSONObject.getString("Signature")));
             }
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
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