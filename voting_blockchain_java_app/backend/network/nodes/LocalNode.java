package network.nodes;

import CryptographyUtils;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPair;

public class LocalNode extends Node {
    // Inherited Fields

    /*
    protected final String host;
    protected final int port;
    protected final PublicKey publicKey;
     */

    // Introduced Fields
    private final PrivateKey privateKey;

    // Initialisation
    public LocalNode(String host, int port) {
        super(host, port);

        KeyPair keyPair = CryptographyUtils.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    // Override
    @Override
    public boolean isLocal(){
        return true;
    }
}