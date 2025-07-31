package network;

// Imports

// Local packages
import network.nodes.RemoteNode;
import util.FileHandlingUtils;

// Imported packages
import org.json.JSONArray;
import org.json.JSONObject;
import util.ParserUtils;

// Included packages
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.io.File;


public class knownPeers{
    // Fields
    private final List<RemoteNode> knownPeers = new ArrayList<>();
    private final File persistentStorage = new File("data/knownPeers.json");

    // Local Variable
    int maxPeers = 8;

    // Initialisation
    public knownPeers() {
        load();
    }

    // Getter
    public List<RemoteNode> getKnownNodes(){
        return knownPeers;
    }

    // Persistent Methods
    private void persist(){
        JSONArray jsonArray = new JSONArray();
        JSONObject tempObject = new JSONObject();
        for (RemoteNode node : knownPeers){
            tempObject.put("Host", node.getHost());
            tempObject.put("Port",  node.getPort());
            tempObject.put("PublicKey",  node.getPublicKey());
            jsonArray.put(tempObject);
        }
        FileHandlingUtils.writeToJSONFile(persistentStorage.getPath(), jsonArray);
    }

    private void load(){
        try {
            JSONArray jsonArray = (JSONArray) FileHandlingUtils.readFromJSONFile(persistentStorage.getPath());
            assert jsonArray != null : "No content to load from";
            for (Object tempObject : jsonArray) {
                JSONObject tempJSONObject = (JSONObject) tempObject;
                knownPeers.add(ParserUtils.JSONObjectToNode(tempJSONObject));
            }
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }
    }

    // Standard Methods
    public boolean isEmpty(){
        return knownPeers.isEmpty();
    }

    public boolean isFull(){
        return knownPeers.size() == maxPeers;
    }

    public boolean containsPeer(RemoteNode node){
        return knownPeers.contains(node);
    }

    public void addPeer(RemoteNode node){
        knownPeers.add(node);
        persist();
    }

    public boolean removePeer(RemoteNode node){
        if (knownPeers.remove(node)) {
            persist();
            return true;
        }
        return false;
    }
}