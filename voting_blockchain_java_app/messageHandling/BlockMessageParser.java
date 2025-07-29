package network.messageHandling;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import blockchain.Block;
import blockchain.Vote;

public class BlockMessageParser{
    public static Block JSONToBlock(JSONObject data) throws Exception {
        String hash = data.getString("hash");
        String previousHash = data.getString("previousHash");
        long timestamp = data.getLong("timestamp");
        int nonce = data.getInt("nonce");

        JSONArray votesJson = data.getJSONArray("votes");
        List<Vote> votes = new ArrayList<>();

        for (int i = 0; i < votesJson.length(); i++){
            JSONObject voteJson = votesJson.getJSONObject(i);
            Vote newVote = VoteMessageParser.JSONToVote(voteJson);
            votes.add(newVote);
        }

        return new Block(previousHash, votes, hash, timestamp, nonce);
    }

    public static JSONObject BlockToJSON(Block block) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("hash", block.getHash());
        jsonObject.put("previousHash", block.getPreviousHash());
        jsonObject.put("timestamp", block.getTimestamp());
        jsonObject.put("nonce", block.getNonce());

        JSONArray voteArray = new JSONArray();
        for (Vote vote: block.getVotes()) {
            voteArray.put(VoteMessageParser.VoteToJSON(vote));
        }

        jsonObject.put("votes", voteArray);

        return jsonObject;
    }
}