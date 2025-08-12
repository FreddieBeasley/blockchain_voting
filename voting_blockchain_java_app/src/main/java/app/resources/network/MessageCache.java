package app.resources.network;

import java.util.LinkedList;
import java.util.Queue;

public class MessageCache {
    private final Queue<String> hashes;
    private final int max_length;

    public MessageCache() {
        this.hashes = new LinkedList<>();
        this.max_length = 100;
    }

    public MessageCache(Queue<String> hashes, int max_length) {
        this.hashes = hashes;
        this.max_length = max_length;
    }

    // Getters
    public int getMaxLength() {
        return max_length;
    }

    public Queue<String> getHashes() {
        return hashes;
    }

    // Methods
    public boolean containsHash(String newHash) {
        for (String hash : hashes) {
            if (hash.equals(newHash)) {
                return true;
            }
        }

        return false;
    }

    public void addHash(String newHash) {
        hashes.add(newHash);
        if (hashes.size() >= max_length) {
            hashes.remove();
        }
    }

}
