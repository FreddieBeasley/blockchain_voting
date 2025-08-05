package resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import resources.network.KnownPeers;
import resources.util.Cryptography;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

public class NetworkManager {
    private final Logger logger;

    private final String host;
    private final int port;

    private final String publicKey;
    private final String privateKey;

    private final KnownPeers knownPeers;

    public NetworkManager(String host, int port, KnownPeers knownPeers) throws NoSuchAlgorithmException{
        this.logger = LoggerFactory.getLogger(NetworkManager.class);

        this.host = host;
        this.port = port;
        this.knownPeers = knownPeers;

        KeyPair keypair = Cryptography.generateKeyPair();
        this.publicKey = Cryptography.publicKeyToString(keypair.getPublic());
        this.privateKey = Cryptography.privateKeyToString(keypair.getPrivate());
    }

    public NetworkManager(String host, int port, KnownPeers knownPeers, String publicKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.logger = LoggerFactory.getLogger(NetworkManager.class);

        this.host = host;
        this.port = port;
        this.knownPeers = knownPeers;

        // Ensures valid public and private key has been passed in
        PublicKey publicKey1 = Cryptography.stringToPublicKey(publicKey); // Throws NoSuchAlgorithmException, InvalidKeySpecException
        PublicKey privateKey1 = Cryptography.stringToPublicKey(privateKey); // Throws NoSuchAlgorithmException, InvalidKeySpecException

        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    // Getters
    public String getHost(){
        return host;
    }

    public int getPort(){
        return port;
    }

    public String getPublicKey(){
        return publicKey;
    }

    public String getPrivateKey(){
        return privateKey;
    }

    public PublicKey getCryptographicPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Cryptography.stringToPublicKey(publicKey);
    }

    public PrivateKey getCryptographicPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Cryptography.stringToPrivateKey(privateKey);
    }

    public KnownPeers getKnownPeers() {
        return knownPeers;
    }



}

