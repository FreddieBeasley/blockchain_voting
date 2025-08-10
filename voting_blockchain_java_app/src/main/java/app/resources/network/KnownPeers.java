package app.resources.network;

// Imports

import app.resources.exceptions.ArchivedException;
import app.resources.exceptions.OverflowException;
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

    public int getMaxPeers() {
        return maxPeers;
    }

    public int getSize() {
        return knownPeers.size();
    }

    public RemotePeer getPeer(int index){
        return knownPeers.get(index);
    }

    public boolean isFull() {
        return knownPeers.size() == maxPeers;
    }

    // Methods
    public void addPeer(RemotePeer node) throws ArchivedException, OverflowException {
        if (knownPeers.contains(node)) {
            throw new ArchivedException("Peer is already in known peers");
        }

        if (isFull()) {
            throw new OverflowException("Known peers is full");
        }

        knownPeers.add(node);
    }

    public boolean removePeer(RemotePeer node) {
        return knownPeers.remove(node);
    }

}