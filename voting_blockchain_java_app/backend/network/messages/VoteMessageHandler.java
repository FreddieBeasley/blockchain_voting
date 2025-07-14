package network.messages;

import blockchain.Vote;
import util.CryptographyUtils;

import java.security.PublicKey;
import java.security.Signature;

import org.json.JSONObject;

public class VoteMessageHandler {

    public static Vote handle(JSONObject data) throws exception {
        PublicKey voter = cryptographyUtils(stringToPublicKey(data.getString("voter")));
        String vote = data.getString("vote");
        String signature = data.getString("signature");

        Vote newVote = Vote(voter, vote, signature);

        return newVote
    }
}
