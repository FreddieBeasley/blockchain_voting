package app.resources.network.resources;

import app.resources.exceptions.InvalidException;
import app.resources.util.Cryptography;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class RemotePeer{

    private final String host;
    private final int port;
    private final String publicKey;

    // Initialisation
    public RemotePeer(String host, int port, String publicKey) throws InvalidException {
        this.host = host;
        this.port = port;
        this.publicKey = publicKey;
    }

    // Getters
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPublicKey(){
        return publicKey;
    }

    @Override
    public boolean equals(Object o) {
        RemotePeer remotePeer = (RemotePeer) o;
        return (getHost().equals(remotePeer.getHost())) && (getPort() == remotePeer.getPort()) && (getPublicKey().equals(remotePeer.getPublicKey()));
    }
}