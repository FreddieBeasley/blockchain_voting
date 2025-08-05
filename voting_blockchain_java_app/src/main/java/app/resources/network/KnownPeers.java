package app.resources.network;

// Imports

import app.resources.network.resources.RemotePeer;

import java.util.ArrayList;
import java.util.List;


public class KnownPeers {
    // Fields
    private final List<RemotePeer> knownPeers;
    private final int maxPeers;

    // Load new known peers list
    public KnownPeers() {
         this.knownPeers = new ArrayList<>();
         this.maxPeers = 8;
    }

    // Load existing known peers list
    public KnownPeers(List<RemotePeer> knownPeers,  int maxPeers) {
        this.knownPeers = knownPeers;
        this.maxPeers = maxPeers;
    }

    // Getter
    public List<RemotePeer> getKnownPeers() {
        return knownPeers;
    }

    public RemotePeer getKnownPeer(int index) {
        return knownPeers.get(index);
    }

    public int getMaxPeers() {
        return maxPeers;
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