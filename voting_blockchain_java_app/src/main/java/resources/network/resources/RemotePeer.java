package resources.network.resources;

import resources.exceptions.InvalidPublicKeyException;
import resources.util.Cryptography;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class RemotePeer{

    private final String host;
    private final int port;
    private final PublicKey publicKey;

    // Initialisation
    public RemotePeer(String host, int port, String publicKey) throws InvalidPublicKeyException {
        this.host = host;
        this.port = port;

        try {
            this.publicKey = Cryptography.stringToPublicKey(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidPublicKeyException("");
        }
    }

    // Getters
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getID(){
     return getHost() + ":" + getPort();
    }

    public String getPublicKeyString(){
        return Cryptography.publicKeyToString(publicKey);
    }
    public PublicKey getPublicKey(){
        return publicKey;
    }
}