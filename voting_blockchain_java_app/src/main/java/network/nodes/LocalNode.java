package network.nodes;

import util.CryptographyUtils;
import util.ParserUtils;

import java.security.PrivateKey;
import java.security.KeyPair;

import org.json.JSONObject;

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
    public LocalNode(String host, int port) throws Exception {
        super(host, port);

        KeyPair keyPair = CryptographyUtils.generateKeyPair();
        this.publicKey = CryptographyUtils.publicKeyToString(keyPair.getPublic());
        this.privateKey = keyPair.getPrivate();
    }

    // Messages
    public String signMessage(JSONObject data) throws Exception {
        // Validate data
        String serialiseVote = ParserUtils.JSONToVote(data).serialise();
        return CryptographyUtils.sign(serialiseVote, privateKey);
    }

    // Override
    @Override
    public boolean isLocal(){
        return true;
    }
}