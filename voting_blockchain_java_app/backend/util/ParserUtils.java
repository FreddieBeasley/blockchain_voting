package util;

import blockchain.Blockchain;
import blockchain.Block;
import blockchain.Vote;

import network.nodes.RemoteNode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParserUtils {
    // Votes
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

    // Blocks
    public static Block JSONToBlock(JSONObject data) throws Exception {
        String hash = data.getString("hash");
        String previousHash = data.getString("previousHash");
        long timestamp = data.getLong("timestamp");
        int nonce = data.getInt("nonce");

        List<Vote> voteList = new ArrayList<>();
        for (Object object: data.getJSONArray("votes")){
            JSONObject voteJson = (JSONObject) object;
            Vote newVote = JSONToVote(voteJson);
            voteList.add(newVote);
        }

        return new Block(previousHash, voteList, hash, timestamp, nonce);
    }

    public static JSONObject BlockToJSON(Block block) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("hash", block.getHash());
        jsonObject.put("previousHash", block.getPreviousHash());
        jsonObject.put("timestamp", block.getTimestamp());
        jsonObject.put("nonce", block.getNonce());

        JSONArray voteArray = new JSONArray();
        for (Vote vote: block.getVotes()) {
            voteArray.put(VoteToJSON(vote));
        }

        jsonObject.put("votes", voteArray);

        return jsonObject;
    }

    // Blockchains
    public static Blockchain JSONToBlockchain(JSONObject data) throws Exception {
        // Difficulty
        int difficulty = data.getInt("difficulty");

        // Chain
        List<Block> blockList = new ArrayList<>();
        for (Object object: data.getJSONArray("chain")){
            JSONObject JSONBlock = (JSONObject) object;
            Block newBlock = JSONToBlock(JSONBlock);
            blockList.add(newBlock);
        }
        // Remaining Voters
        Set<PublicKey> remainingVoters = new HashSet<>();
        for (Object object: data.getJSONArray("remainingVoters")){
            PublicKey voter = (PublicKey) object;
            remainingVoters.add(voter);
        }

        return new Blockchain(blockList, difficulty, remainingVoters);
    }

    public static JSONObject BlockchainToJSON(Blockchain blockchain) throws Exception {
        JSONObject jsonObject = new JSONObject();

        // Difficulty
        jsonObject.put("difficulty", blockchain.getDifficulty());

        // Chain
        JSONArray JSONChain = new JSONArray();
        for (Block block: blockchain.getChain()) {
            JSONChain.put(BlockToJSON(block));
        }
        jsonObject.put("chain", JSONChain);

        // Remaining Voters
        JSONArray JSONRemainingVoters = new JSONArray();
        for (PublicKey voter: blockchain.getRemainingVoters()) {
            JSONRemainingVoters.put(voter);
        }
        jsonObject.put("remainingVoters", JSONRemainingVoters);

        return jsonObject;
    }

    // Peers
    public static RemoteNode JSONObjectToNode(JSONObject data) throws Exception {
        String host = data.getString("host");
        int port = data.getInt("port");
        PublicKey publicKey = CryptographyUtils.stringToPublicKey(data.getString("publicKey"));

        return new RemoteNode(host, port, publicKey);
    }

    public static JSONObject nodeToJSONObject(RemoteNode node) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("host", node.getHost());
        jsonObject.put("port", node.getPort());
        jsonObject.put("publicKey", CryptographyUtils.publicKeyToString(node.getPublicKey()));

        return jsonObject;
    }
}