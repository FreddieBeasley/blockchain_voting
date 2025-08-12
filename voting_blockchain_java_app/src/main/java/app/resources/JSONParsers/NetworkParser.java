package app.resources.JSONParsers;

import app.resources.exceptions.InvalidException;
import app.resources.network.MessageCache;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.LocalNode;
import app.resources.NetworkManager;
import app.resources.exceptions.MalformedJSONException;
import app.resources.network.KnownPeers;
import app.resources.network.resources.RemotePeer;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class NetworkParser {
    // Remote Peer
    public static RemotePeer JSONToRemotePeer(JSONObject data) throws MalformedJSONException, InvalidException {
        if (!data.has("host")){
            throw new MalformedJSONException("Required field 'host' missing");
        }
        if (!data.has("port")){
            throw new MalformedJSONException("Required field 'port' missing");
        }
        if (!data.has("public_key")){
            throw new MalformedJSONException("Required field 'publicKey' missing");
        }

        String host;
        int port;
        String publicKey;

        try {
            host = data.getString("host");
        }  catch (JSONException e) {
            throw new MalformedJSONException("Required field 'host' is malformed", e);
        }

        try {
            port = data.getInt("port");
        }  catch (JSONException e) {
            throw new MalformedJSONException("Required field 'port' is malformed", e);
        }

        try {
            publicKey = data.getString("public_key");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'publicKey' is malformed", e);
        }

        return new RemotePeer(host, port, publicKey);
    }

    public static JSONObject remotePeerToJSON(RemotePeer node){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("host", node.getHost());
        jsonObject.put("port", node.getPort());
        jsonObject.put("public_key", node.getPublicKey());

        return jsonObject;
    }

    // Known Peers Set
    public static Set<RemotePeer> JSONToRemotePeerSet(JSONArray data) throws MalformedJSONException, InvalidException {
        Set<RemotePeer> remotePeerList = new HashSet<>();
        for (Object o : data) {
            JSONObject JSONRemotePeer = (JSONObject) o;
            RemotePeer remotePeer = JSONToRemotePeer(JSONRemotePeer);
            remotePeerList.add(remotePeer);
        }
        return remotePeerList;
    }

    public static JSONArray remotePeerSetToJSON(Set<RemotePeer> remotePeers) {
        JSONArray jsonKnownPeers = new JSONArray();
        for (RemotePeer remotePeer : remotePeers) {
            JSONObject JSONRemotePeer = remotePeerToJSON(remotePeer);
            jsonKnownPeers.put(JSONRemotePeer);
        }
        return jsonKnownPeers;
    }

    // Hashes Set
    public static Queue<String> JSONToHashesQueue(JSONArray data) {
        Queue<String> hashes = new LinkedList<>();
        for (Object o : data) {
            String hash = (String) o;
            hashes.add(hash);
        }
        return hashes;
    }

    public static JSONArray hashesQueueToJSON(Queue<String> hashes) {
        JSONArray jsonHashes = new JSONArray();
        for (String hash : hashes) {
            jsonHashes.put(hash);
        }
        return jsonHashes;
    }

    // Known Peers
    public static KnownPeers JSONToKnownPeers(JSONObject data) throws MalformedJSONException {
        if (!data.has("max_peers")){
            throw new MalformedJSONException("Required field 'max_peers' missing");
        }

        if (!data.has("known_peers_list")){
            throw new MalformedJSONException("Required field 'known_peers_list' missing");
        }

        int maxPeers;
        Set<RemotePeer> knownPeers;

        try {
            maxPeers = data.getInt("max_peers");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'max_peers' is malformed", e);
        }

        try {
            knownPeers = JSONToRemotePeerSet(data.getJSONArray("known_peers_list"));
        } catch (JSONException | InvalidException e) {
            throw new MalformedJSONException("Required field 'known_peers_list' is malformed", e);
        }

        return new KnownPeers(knownPeers, maxPeers);
    }

    public static JSONObject knownPeersToJSON(KnownPeers knownPeers) {
        JSONObject jsonObject = new JSONObject();

        // MaxPeers
        jsonObject.put("max_peers", knownPeers.getMaxPeers());

        JSONArray JSONKnownPeers = remotePeerSetToJSON(knownPeers.getKnownPeers());
        jsonObject.put("known_peers_list", JSONKnownPeers);

        return jsonObject;

    }

    // Message Cache
    public static MessageCache JSONToMessageCache(JSONObject data ) throws MalformedJSONException {
        if (!data.has("max_length")){
            throw new MalformedJSONException("Required field 'max_peers' missing");
        }

        if (!data.has("hashes")){
            throw new MalformedJSONException("Required field 'known_peers_list' missing");
        }

        int maxLength;
        Queue<String> hashes;

        try {
            maxLength = data.getInt("max_length");
        }  catch (JSONException e) {
            throw new MalformedJSONException("Required field 'max_length' is malformed", e);
        }

        try {
            hashes = JSONToHashesQueue(data.getJSONArray("hashes"));
        }  catch (JSONException e) {
            throw new MalformedJSONException("Required field 'hashes' is malformed", e);
        }

        return new MessageCache(hashes, maxLength);
    }

    public static JSONObject messageCacheToJSON(MessageCache messageCache) {
        JSONObject jsonObject = new JSONObject();

        // Max_length
        jsonObject.put("max_length", messageCache.getMaxLength());

        JSONArray JSONHashes = hashesQueueToJSON(messageCache.getHashes());
        jsonObject.put("hashes", JSONHashes);

        return jsonObject;
    }

    // Public Keys & Private Keys do not need to be converted

    // Does not load or store port and host - this gets passed in from the localNode object
    public static NetworkManager  JSONToNetworkManager(JSONObject data, String host, int port, LocalNode localNode) throws MalformedJSONException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (!data.has("public_key")){
            throw new MalformedJSONException("Required field 'publicKey' missing");
        }

        if (!data.has("private_key")){
            throw new MalformedJSONException("Required field 'privateKey' missing");
        }

        if (!data.has("known_peers")){
            throw new MalformedJSONException("Required field 'known_peers' missing");
        }

        if (!data.has("message_cache")){
            throw new MalformedJSONException("Required field 'message_cache' missing");
        }

        String publicKey;
        String privateKey;
        KnownPeers knownPeers;
        MessageCache messageCache;

        try {
            publicKey = data.getString("public_key");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'publicKey' is malformed", e);
        }

        try {
            privateKey = data.getString("private_key");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'privateKey' is malformed", e);
        }

        try {
            knownPeers = JSONToKnownPeers(data.getJSONObject("known_peers"));
        }  catch (JSONException e) {
            throw new MalformedJSONException("Required field 'known_peers' is malformed", e);
        }

        try {
            messageCache = JSONToMessageCache(data.getJSONObject("message_cache"));
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'message_cache' is malformed", e);
        }

        try {
            return new NetworkManager(host, port, localNode, knownPeers, messageCache, publicKey, privateKey);
        } catch (InvalidException e) {
            return new NetworkManager(host, port, localNode, knownPeers, messageCache);
        }
    }

    public static JSONObject networkManagerToJSON(NetworkManager networkManager) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("public_key", networkManager.getPublicKey());
        jsonObject.put("private_key", networkManager.getPrivateKey());
        jsonObject.put("known_peers", knownPeersToJSON(networkManager.getKnownPeers()));
        jsonObject.put("message_cache", messageCacheToJSON(networkManager.getMessageCache()));
        return jsonObject;
    }

}