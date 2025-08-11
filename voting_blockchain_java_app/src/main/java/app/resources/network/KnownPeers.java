package app.resources.network;

// Imports

import app.resources.exceptions.ArchivedException;
import app.resources.exceptions.OverflowException;
import app.resources.network.resources.RemotePeer;

import java.util.*;


public class KnownPeers {
    // Fields
    private final Set<RemotePeer> knownPeers;
    private final int maxPeers;

    // Load new known peers list
    public KnownPeers() {
         this.knownPeers = new HashSet<>();
         this.maxPeers = 8;
    }

    // Load existing known peers list
    public KnownPeers(Set<RemotePeer> knownPeers,  int maxPeers) {
        this.knownPeers = knownPeers;
        this.maxPeers = maxPeers;
    }

    // Getter
    public Set<RemotePeer> getKnownPeers() {
        return knownPeers;
    }

    public int getMaxPeers() {
        return maxPeers;
    }

    public int getSize() {
        return knownPeers.size();
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

    public boolean containsPeer(RemotePeer node) {
        return knownPeers.contains(node);
    }

    public Set<RemotePeer> getRandomPeers(int count) {
        if (count > getSize()) {
            count = getSize();
        }

        List<RemotePeer> peersList = new ArrayList<>(getKnownPeers());
        Collections.shuffle(peersList); // randomize order
        return new HashSet<>(peersList.subList(0, count));
    }

}