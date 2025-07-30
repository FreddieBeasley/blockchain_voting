package network.nodes;

import java.security.PublicKey;

public class RemoteNode extends Node {
    // Inherited Fields

    /*
    protected final String host;
    protected final int port;
    protected final PublicKey publicKey;
     */

    // Initialisation
    public RemoteNode(String host, int port, PublicKey publicKey) {
        super(host, port);

        this.publicKey = publicKey;
    }

    // Override
    @Override
    public boolean isLocal(){
        return false;
    }

}