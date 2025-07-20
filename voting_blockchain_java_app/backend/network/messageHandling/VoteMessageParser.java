package network.messageHandling;

import blockchain.Vote;
import util.CryptographyUtils;
import org.json.*;
import java.security.PublicKey;

public class VoteMessageParser {
    public static Vote JSONToVote(JSONObject data) throws Exception {
        // Validation
        if (!data.has("voter")) {
            throw new Exception("JSON missing voter public key");
        } else if (!data.has("voteValue")) {
            throw new Exception("JSON missing vote choice");
        } else if (!data.has("signature")){
            throw new Exception("JSON missing signature");
        }

        PublicKey voter = CryptographyUtils.stringToPublicKey(data.getString("voter"));
        int voteValue = data.getInt("voteValue");
        String signature = data.getString("signature");

        return new Vote(voter, voteValue, signature);
    }

    public static JSONObject VoteToJSON(Vote vote) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("voter", CryptographyUtils.publicKeyToString(vote.getVoter()));
        jsonObject.put("voteValue", vote.getVoteValue());
        jsonObject.put("signature", vote.getSignature());

        return jsonObject;
    }


}