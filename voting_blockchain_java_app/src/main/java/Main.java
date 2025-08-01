import blockchain.*;

import util.ParserUtils;
import util.CryptographyUtils;
import util.FileHandlingUtils;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static KeyPair register() throws Exception{
        KeyPair keypair = CryptographyUtils.generateKeyPair();
        if (keypair == null){
            logger.error("Failed to generate keypair");
            return null;
        }

        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        FileHandlingUtils.appendToJSONFileArray("src/main/data/registeredVoters.json", CryptographyUtils.publicKeyToString(publicKey));
        logger.info("Key pair generated with public key: " + CryptographyUtils.publicKeyToString(publicKey));
        return keypair;
    }

    public static Vote createVote(PublicKey publicKey, PrivateKey privateKey, int voteValue) throws Exception{
        Vote newVote = new Vote(CryptographyUtils.publicKeyToString(publicKey),voteValue);
        newVote.signVote(privateKey);
        logger.info("Vote created with public key: " + CryptographyUtils.publicKeyToString(publicKey));
        return newVote;
    }

    public static void main(String[] args) throws Exception{
        logger.info("main started");

        System.out.println(FileHandlingUtils.readFromJSONFile("src/main/data/blockchain.json"));

        KeyPair myKeys;
        myKeys = register();

        PublicKey myPublicKey = myKeys.getPublic();
        PrivateKey myPrivateKey = myKeys.getPrivate();

        Blockchain blockchain = new Blockchain();


        logger.info("Last block of blockchain: " + ParserUtils.BlockToJSON(blockchain.getLastBlock()));

        Vote myVote = createVote(myPublicKey,myPrivateKey,3);
        blockchain.addNewVote(myVote);

        blockchain.createNewBlock();
        logger.info("Newly mined block hash: " + blockchain.getLastBlock().getHash());
    }
}