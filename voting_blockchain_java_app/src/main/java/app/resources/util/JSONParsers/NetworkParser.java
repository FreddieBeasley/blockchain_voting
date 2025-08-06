package app.resources.util.JSONParsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.LocalPeer;
import app.resources.NetworkManager;
import app.resources.exceptions.InvalidPublicKeyException;
import app.resources.exceptions.MalformedJSONException;
import app.resources.network.KnownPeers;
import app.resources.network.resources.RemotePeer;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class NetworkParser {
    // Remote Peer
    public static RemotePeer JSONToRemotePeer(JSONObject data) throws MalformedJSONException {
        if (!data.has("host")){
            throw new MalformedJSONException("Required field 'host' missing");
        }
        if (!data.has("port")){
            throw new MalformedJSONException("Required field 'port' missing");
        }
        if (!data.has("publicKey")){
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
            publicKey = data.getString("publicKey");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'publicKey' is malformed", e);
        }

        try {
            return new RemotePeer(host, port, publicKey);
        } catch (InvalidPublicKeyException e) {
            throw new MalformedJSONException("Remote Peer has an Invalid Public Key", e);
        }
    }

    public static JSONObject remotePeerToJSON(RemotePeer node){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("host", node.getHost());
        jsonObject.put("port", node.getPort());
        jsonObject.put("publicKey", node.getPublicKey());

        return jsonObject;
    }

    // Known Peers List
    public static List<RemotePeer> remotePeerListToJSON(JSONArray data) throws MalformedJSONException {
        List<RemotePeer> remotePeerList = new ArrayList<>();
        for (Object o : data) {
            JSONObject JSONRemotePeer = (JSONObject) o;
            RemotePeer remotePeer = JSONToRemotePeer(JSONRemotePeer);
            remotePeerList.add(remotePeer);
        }
        return remotePeerList;
    }

    public static JSONArray remotePeerListToJSON(List<RemotePeer> data) {
        JSONArray jsonKnownPeers = new JSONArray();
        for (RemotePeer remotePeer : data) {
            jsonKnownPeers.put(remotePeer);
        }
        return jsonKnownPeers;
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
        List<RemotePeer>  knownPeers;

        try {
            maxPeers = data.getInt("max_peers");
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'max_peers' is malformed", e);
        }

        try {
            knownPeers = remotePeerListToJSON(data.getJSONArray("known_peers_list"));
        } catch (JSONException e) {
            throw new MalformedJSONException("Required field 'known_peers_list' is malformed", e);
        }

        return new KnownPeers(knownPeers, maxPeers);
    }

    public static JSONObject knownPeersToJSON(KnownPeers knownPeers) {
        JSONObject jsonObject = new JSONObject();

        // MaxPeers
        jsonObject.put("max_peers", knownPeers.getMaxPeers());

        JSONArray JSONKnownPeers = remotePeerListToJSON(knownPeers.getKnownPeers());
        jsonObject.put("known_peers_list", JSONKnownPeers);

        return jsonObject;

    }

    // Public Keys & Private Keys do not need to be converted

    // Does not load or store port and host - this gets passed in from the localNode object
    public static NetworkManager  JSONToNetworkManager(JSONObject data, String host, int port, LocalPeer localPeer) throws MalformedJSONException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (!data.has("public_key")){
            throw new MalformedJSONException("Required field 'publicKey' missing");
        }

        if (!data.has("private_key")){
            throw new MalformedJSONException("Required field 'privateKey' missing");
        }

        if (!data.has("known_peers")){
            throw new MalformedJSONException("Required field 'known_peers' missing");
        }

        String publicKey;
        String privateKey;
        KnownPeers knownPeers;

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

        return new NetworkManager(host, port, localPeer, knownPeers);
    }

    public static JSONObject networkManagerToJSON(NetworkManager networkManager) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("public_key", networkManager.getPublicKey());
        jsonObject.put("private_key", networkManager.getPrivateKey());
        jsonObject.put("known_peers", knownPeersToJSON(networkManager.getKnownPeers()));
        return jsonObject;
    }

}