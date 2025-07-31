package network.nodes;

import util.CryptographyUtils;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public abstract class Node {

    // Fields
    protected final String host;
    protected final int port;
    protected String publicKey;

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

    public String getNodeID() {
        return getHost() + ":" + getPort();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public PublicKey getPublicKeyAsPublicKey() {
        try {
            return CryptographyUtils.stringToPublicKey(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new RuntimeException(e);
        }
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
        Node node = (Node) o;
        return getNodeID().equals(node.getNodeID());
    }

}