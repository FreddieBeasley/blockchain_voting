package network.messages;

import blockchain.Block;
import blockchain.Vote;

import org.json.JSONObject
import org.json.JSONArray

public class BlockMessageHandler {

    public static Block handle(JSONObject data) throws exception {
        String hash = data.getString("hash");
        String previousHash = data.getString("previous_hash");
        Long timestamp = data.getLong("timestamp");
        Integer nonce = data.getInt("nonce");

        JSONARRAY votesJson = data.getJSONArray("votes");
        List<Vote> votes = new ArrayList<>();

        for (int i = 0; i < votesJson.length(), i++){
            JSONOBJECT voteJson = votesJson.getJSONObject(i);
            newVote = VoteMessageHandler.handle(voteJson);
            votes.add(newVote);
        }

        Block newBlock = Block(previousHash, votes, hash, timestamp, nonce);

        return newBlock;

    }


}