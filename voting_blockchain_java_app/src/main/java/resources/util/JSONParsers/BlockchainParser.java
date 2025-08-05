package resources.util.JSONParsers;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import resources.Blockchain;
import resources.blockchain.resources.Block;
import resources.blockchain.resources.Vote;
import resources.exceptions.MalformedJSONException;

import java.util.*;


public class BlockchainParser {
    // Votes
    public static Vote JSONToVote(JSONObject data) throws MalformedJSONException {

        if (!data.has("voter")) {
            throw new MalformedJSONException("VOTE: Required field 'voter' missing");
        }
        if (!data.has("voteValue")) {
            throw new MalformedJSONException("VOTE: Required field 'voteValue' missing");
        }

        String voter;
        int voteValue;

        try {
            voter = data.getString("voter");
        } catch (JSONException e) {
            throw new MalformedJSONException("VOTE: Required field 'voter' is malformed");
        }
        try {
            voteValue = data.getInt("voteValue");
        } catch (JSONException e) {
            throw new MalformedJSONException("VOTE: Required field 'voteValue' is malformed");
        }


        if (!data.has("signature")) {
            return new Vote(voter, voteValue);
        }

        String signature = data.getString("signature");
        return new Vote(voter, voteValue, signature);
    }

    public static JSONObject VoteToJSON(Vote vote) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("voter", vote.getVoter());
        jsonObject.put("voteValue", vote.getVoteValue());

        if (vote.getSignature() != null) {
            jsonObject.put("signature", vote.getSignature());
        }

        return jsonObject;
    }

    // Blocks
    public static Block JSONToBlock(JSONObject data) throws MalformedJSONException {
        if (!data.has("hash")) {
            throw new MalformedJSONException("BLOCK: Required field 'hash' missing");
        }
        if (!data.has("previousHash")) {
            throw new MalformedJSONException("BLOCK: Required field 'previousHash' missing");
        }
        if (!data.has("timestamp")) {
            throw new MalformedJSONException("BLOCK: Required field 'timestamp' missing");
        }
        if (!data.has("nonce")) {
            throw new MalformedJSONException("BLOCK: Required field 'nonce' missing");
        }
        if (!data.has("votes")) {
            throw new MalformedJSONException("BLOCK: Required field 'votes' missing");
        }

        String hash;
        String prevHash;
        long timestamp;
        int nonce;
        List<Vote> voteList;

        try {
            hash = data.getString("hash");
        } catch (JSONException e) {
            throw new MalformedJSONException("BLOCK: Required field 'hash' is malformed", e);
        }
        try {
            prevHash = data.getString("previousHash");
        } catch (JSONException e) {
            throw new MalformedJSONException("BLOCK: Required field 'previousHash' is malformed", e);
        }
        try {
            timestamp = data.getLong("timestamp");
        } catch (JSONException e) {
            throw new MalformedJSONException("BLOCK: Required field 'timestamp' is malformed", e);
        }
        try {
            nonce = data.getInt("nonce");
        } catch (JSONException e) {
            throw new MalformedJSONException("BLOCK: Required field 'nonce' is malformed", e);
        }


        try {
            voteList = new ArrayList<>();
            for (Object object : data.getJSONArray("votes")) {
                JSONObject voteJson = (JSONObject) object;
                Vote newVote;

                newVote = JSONToVote(voteJson);

                voteList.add(newVote);
            }
        } catch (JSONException | MalformedJSONException e) {
            throw new MalformedJSONException("BLOCK: Required field 'votes' is malformed", e);
        }

        return new Block(prevHash, voteList, hash, timestamp, nonce);
    }

    public static JSONObject BlockToJSON(Block block) {
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

    // Blockchain
    public static resources.Blockchain JSONToBlockchain(JSONObject data) throws MalformedJSONException {
        if (!data.has("chain")) {
            throw new MalformedJSONException("Required field 'chain' missing");
        }

        if (!data.has("remainingVoters")) {
            throw new MalformedJSONException("Required field 'remainingVoters' missing");
        }

        if (!data.has("pendingVotes")) {
            throw new MalformedJSONException("Required field 'pendingVotes' missing");
        }

        if (!data.has("difficulty")) {
            throw new MalformedJSONException("Required field 'difficulty' missing");
        }

        List<Block> chain;
        Set<String> remainingVoters;
        Queue<Vote> pendingVotes;
        int difficulty;

        try {
            chain = JSONToChain(data.getJSONArray("chain"));
        } catch (MalformedJSONException e) {
            throw new MalformedJSONException("Required field 'chain' is malformed", e);
        }

        try {
            remainingVoters = JSONToRemainingVoters(data.getJSONArray("remainingVoters"));
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'remainingVoters' is malformed", e);
        }

        try {
            pendingVotes = JSONToPendingVotes(data.getJSONArray("pendingVotes"));
        } catch (MalformedJSONException e) {
            throw new MalformedJSONException("Required field 'pendingVotes' is malformed", e);
        }

        try {
            difficulty = data.getInt("difficulty");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'difficulty' is malformed", e);
        }

        return new Blockchain(chain, remainingVoters, pendingVotes, difficulty);
    }

    public static JSONObject BlockchainToJSON(Blockchain blockchain) {
        JSONObject jsonObject = new JSONObject();

        JSONArray JSONChain = chainToJSON(blockchain.getChain());
        jsonObject.put("chain", JSONChain);

        JSONArray JSONRemainingVoters = remainingVotersToJSON(blockchain.getRemainingVoters());
        jsonObject.put("remainingVoters", JSONRemainingVoters);

        JSONArray JSONPendingVotes = PendingVotesToJSON(blockchain.getPendingVotes());
        jsonObject.put("pendingVotes", JSONPendingVotes);

        jsonObject.put("difficulty", blockchain.getDifficulty());

        return jsonObject;
    }

    // Chain
    public static JSONArray chainToJSON(List<Block> chain) {
        JSONArray JSONChain = new JSONArray();
        for (Block block : chain) {
            JSONChain.put(BlockToJSON(block));
        }
        return JSONChain;
    }

    public static List<Block> JSONToChain(JSONArray JSONChain) throws MalformedJSONException {
        List<Block> chain = new ArrayList<>();
        for (Object object : JSONChain) {
            JSONObject JSONBlock = (JSONObject) object;
            Block newBlock;
            try {
                newBlock = JSONToBlock(JSONBlock);
            } catch (MalformedJSONException e) {
                throw new MalformedJSONException("A block in the chain is malformed", e);
            }
            chain.add(newBlock);
        }
        return chain;
    }

    // Remaining Voters
    public static JSONArray remainingVotersToJSON(Set<String> remainingVoters) {
        JSONArray JSONRemainingVoters = new JSONArray();
        for (String voter : remainingVoters) {
            JSONRemainingVoters.put(voter);
        }
        return JSONRemainingVoters;
    }

    public static Set<String> JSONToRemainingVoters(JSONArray JSONRemainingVoters) {
        Set<String> remainingVoters = new HashSet<>();
        for (Object object : JSONRemainingVoters) {
            String voter = (String) object;
            remainingVoters.add(voter);
        }
        return remainingVoters;
    }

    // Pending Votes
    public static JSONArray PendingVotesToJSON(Queue<Vote> pendingVotes) {
        JSONArray JSONPendingVotes = new JSONArray();
        for (Vote vote : pendingVotes) {
            JSONPendingVotes.put(VoteToJSON(vote));
        }
        return JSONPendingVotes;
    }

    public static Queue<Vote> JSONToPendingVotes(JSONArray JSONPendingVotes) throws MalformedJSONException {
        Queue<Vote> pendingVotes = new LinkedList<>();
        for (Object object : JSONPendingVotes) {
            JSONObject JSONPendingVote = (JSONObject) object;

            Vote pendingVote;

            try {
                pendingVote = JSONToVote(JSONPendingVote);
            } catch (MalformedJSONException e) {
                throw new MalformedJSONException("A vote in the pending votes is malformed", e);
            }

            pendingVotes.add(pendingVote);
        }
        return pendingVotes;
    }
}