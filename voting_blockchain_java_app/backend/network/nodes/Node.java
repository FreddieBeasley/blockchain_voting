package network.nodes;

import java.security.PublicKey;

public abstract class Node {

    // Fields
    protected final String host;
    protected final int port;
    protected PublicKey publicKey;

    // Initialisation
    public Node(String host, int port) {
        this.host = host;
        this.port = port;
        this.publicKey = null;
    }

    // Getters
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    // Methods
    public abstract boolean isLocal();

    // Override
    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }
        Node foreignNode = (Node) o;
        return host.equals(foreignNode.host) && port == foreignNode.port;
    }

}