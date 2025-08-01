package util;

// Local Packages
import blockchain.Blockchain;
import blockchain.Block;
import blockchain.Vote;
import network.nodes.RemoteNode;
import exceptions.*;

// JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Datatypes
import java.util.*; // List, ArrayList, Set, Hashset


public class ParserUtils {

    // Votes
    public static Vote JSONToVote(JSONObject data) throws MalformedJSONVoteException {

        if (!data.has("voter")) {
            throw new MalformedJSONVoteException("Required field 'voter' missing");
        }
        if (!data.has("voteValue")) {
            throw new MalformedJSONVoteException("Required field 'voteValue' missing");
        }

        String voter;
        int voteValue;

        try {
            voter = data.getString("voter");
        } catch (JSONException e) {
            throw new MalformedJSONVoteException("Required field 'voter' is malformed");
        }
        try {
            voteValue = data.getInt("voteValue");
        } catch (JSONException e) {
            throw new MalformedJSONVoteException("Required field 'voteValue' is malformed");
        }


        if (!data.has("signature")) {
            return new Vote(voter, voteValue);
        }

        String signature = data.getString("signature");
        return new Vote(voter, voteValue, signature);
    }

    public static JSONObject VoteToJSON(Vote vote){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("voter", vote.getVoter());
        jsonObject.put("voteValue", vote.getVoteValue());

        if (vote.getSignature() != null) {
            jsonObject.put("signature", vote.getSignature());
        }

        return jsonObject;
    }

    // Blocks
    public static Block JSONToBlock(JSONObject data) throws MalformedJSONBlockException {
        if (!data.has("hash")) {
            throw new MalformedJSONBlockException("Required field 'hash' missing");
        }
        if (!data.has("previousHash")) {
            throw new MalformedJSONBlockException("Required field 'previousHash' missing");
        }
        if (!data.has("timestamp")) {
            throw new MalformedJSONBlockException("Required field 'timestamp' missing");
        }
        if (!data.has("nonce")) {
            throw new MalformedJSONBlockException("Required field 'nonce' missing");
        }
        if (!data.has("votes")) {
            throw new MalformedJSONBlockException("Required field 'votes' missing");
        }

        String hash;
        String prevHash;
        long timestamp;
        int nonce;
        List<Vote> voteList;

        try {
            hash = data.getString("hash");
        } catch (JSONException e) {
            throw new MalformedJSONBlockException("Required field 'hash' is malformed");
        }
        try {
            prevHash = data.getString("previousHash");
        } catch (JSONException e) {
            throw new MalformedJSONBlockException("Required field 'previousHash' is malformed");
        }
        try {
            timestamp = data.getLong("timestamp");
        }  catch (JSONException e) {
            throw new MalformedJSONBlockException("Required field 'timestamp' is malformed");
        }
        try{
            nonce = data.getInt("nonce");
        } catch (JSONException e) {
            throw new MalformedJSONBlockException("Required field 'nonce' is malformed");
        }


        try {
            voteList = new ArrayList<>();
            for (Object object : data.getJSONArray("votes")) {
                JSONObject voteJson = (JSONObject) object;
                Vote newVote;

                newVote = JSONToVote(voteJson);

                voteList.add(newVote);
            }
        } catch (JSONException | MalformedJSONVoteException e) {
            throw new MalformedJSONBlockException("Required field 'votes' is malformed");
        }

        return new Block(prevHash, voteList, hash, timestamp, nonce);
    }

    public static JSONObject BlockToJSON(Block block){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("hash", block.getHash());
        jsonObject.put("previousHash", block.getPreviousHash());
        jsonObject.put("timestamp", block.getTimestamp());
        jsonObject.put("nonce", block.getNonce());

        JSONArray voteArray = new JSONArray();
        for (Vote vote : block.getVotes()) {
            voteArray.put(VoteToJSON(vote));
        }

        jsonObject.put("votes", voteArray);

        return jsonObject;
    }

    // Blockchains
    public static Blockchain JSONToBlockchain(JSONObject data) throws MalformedJSONBlockchainException {
        if (!(data instanceof JSONObject)) {
            throw new MalformedJSONBlockchainException("data is malformed");
        }

        // Difficulty
        if (!data.has("difficulty")){
            throw new MalformedJSONBlockchainException("Required field 'difficulty' missing");
        }
        if (!data.has("chain")){
            throw new MalformedJSONBlockchainException("Required field 'chain' missing");
        }
        if (!data.has("remainingVoters")){
            throw new MalformedJSONBlockchainException("Required field 'remainingVoters' missing");
        }

        int difficulty;
        List<Block> blockList;
        Set<String> remainingVoters;

        try {
            difficulty = data.getInt("difficulty");
        } catch (JSONException e) {
            throw new MalformedJSONBlockchainException("Required field 'difficulty' is malformed");
        }

        try {
            blockList = new ArrayList<>();
            for (Object object : data.getJSONArray("chain")) {
                JSONObject JSONBlock = (JSONObject) object;
                Block newBlock;
                newBlock = JSONToBlock(JSONBlock);
                blockList.add(newBlock);
            }
        } catch (JSONException | MalformedJSONBlockException e) {
            throw new MalformedJSONBlockchainException("Required field 'chain' is malformed", e);
        }

        try {
            remainingVoters = new HashSet<>();
            for (Object object : data.getJSONArray("remainingVoters")) {
                String voter = (String) object;
                remainingVoters.add(voter);
            }
        }  catch (JSONException e) {
            throw new MalformedJSONBlockchainException("Required field 'remainingVoters' is malformed", e);
        }

        return new Blockchain(blockList, difficulty, remainingVoters);
    }

    public static JSONObject BlockchainToJSON(Blockchain blockchain) {
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
        for (String voter: blockchain.getRemainingVoters()) {
            JSONRemainingVoters.put(voter);
        }
        jsonObject.put("remainingVoters", JSONRemainingVoters);

        return jsonObject;
    }

    // Peers
    public static RemoteNode JSONObjectToNode(JSONObject data) throws MalformedJSONPeerException  {
        if (!data.has("host")){
            throw new MalformedJSONPeerException("Required field 'host' missing");
        }
        if (!data.has("port")){
            throw new MalformedJSONPeerException("Required field 'port' missing");
        }
        if (!data.has("publicKey")){
            throw new MalformedJSONPeerException("Required field 'publicKey' missing");
        }

        String host;
        int port;
        String publicKey;

        try {
            host = data.getString("host");
        }  catch (JSONException e) {
            throw new MalformedJSONPeerException("Required field 'host' is malformed", e);
        }

        try {
            port = data.getInt("port");
        }  catch (JSONException e) {
            throw new MalformedJSONPeerException("Required field 'port' is malformed", e);
        }

        try {
            publicKey =data.getString("publicKey");
        } catch (JSONException e) {
            throw new MalformedJSONPeerException("Required field 'publicKey' is malformed", e);
        }

        return new RemoteNode(host, port, publicKey);
    }

    public static JSONObject nodeToJSONObject(RemoteNode node){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("host", node.getHost());
        jsonObject.put("port", node.getPort());
        jsonObject.put("publicKey", node.getPublicKey());

        return jsonObject;
    }

    public static List<String> JSONArrayToList(JSONArray data) {
        List<String> list = new ArrayList<>();
        for (Object object: data){
            String key = (String) object;
            list.add(key);
        }
        return list;
    }

}