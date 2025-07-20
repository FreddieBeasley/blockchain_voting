package network;
// Maintains a persistent list of known nodes


import network.nodes.RemoteNode;
import java.util.Set;
import java.util.HashSet;


public class knownPeers{
    // Fields
    private final Set<RemoteNode> knownPeers = new HashSet<>();

    // Getter
    public Set<RemoteNode> getKnownNodes(){
        return knownPeers;
    }

    // Methods
    public boolean isDiscovered(RemoteNode node){
        return knownPeers.contains(node);
    }

    public void discover(RemoteNode node){
        knownPeers.add(node);
    }

    public void detach(RemoteNode node){
        knownPeers.remove(node);}
}