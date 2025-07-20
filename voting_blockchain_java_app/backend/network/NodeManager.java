// Maintains a persistent list of known nodes

package network;

import network.nodes.RemoteNode;
import java.util.Set;
import java.util.HashSet;


public class NodeManager{
    // Fields
    private final Set<RemoteNode> knownNodes = new HashSet<>();

    // Getter
    public Set<RemoteNode> getKnownNodes(){
        return knownNodes;
    }

    // Methods
    public boolean discovered(RemoteNode node){
        if (knownNodes.contains(node)){
            return true;
        }
        return false;
    }

    public void discover(RemoteNode node){
        knownNodes.add(node);
    }

    public void detach(RemoteNode node){
        knownNodes.remove(node);}
}