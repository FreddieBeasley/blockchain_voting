package network;

// Imports

import java.util.ArrayList;
import java.util.List;


public class KnownPeers {
    // Fields
    private final List<RemotePeer> knownPeers;

    // Local Variable
    int maxPeers = 8;

    // Load new known peers list
    public KnownPeers() {
         knownPeers = new ArrayList<>();
    }

    // Load existing known peers list
    public KnownPeers(List<RemotePeer> knownPeers) {
        this.knownPeers = knownPeers;
    }

    // Getter
    public List<RemotePeer> getKnownPeers() {
        return knownPeers;
    }

    public RemotePeer getKnownPeer(int index) {
        return knownPeers.get(index);
    }

    // Standard Methods
    public boolean isEmpty() {
        return knownPeers.isEmpty();
    }

    public boolean isFull() {
        return knownPeers.size() == maxPeers;
    }

    public boolean containsPeer(RemotePeer node) {
        return knownPeers.contains(node);
    }

    public boolean addPeer(RemotePeer node) {
        return knownPeers.add(node);
    }

    public boolean removePeer(RemotePeer node) throws Exception{
        if (knownPeers.remove(node)) {
            return true;
        }
        return false;
    }
}