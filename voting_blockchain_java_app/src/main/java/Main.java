import blockchain.*;

import util.ParserUtils;
import util.CryptographyUtils;
import util.FileHandlingUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static KeyPair register(){
        KeyPair keypair = CryptographyUtils.generateKeyPair();
        if (keypair == null){
            logger.error("KeyPair generation failed");
            return null;
        }

        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        FileHandlingUtils.appendToJSONFileArray("data/registeredVoters.json", CryptographyUtils.publicKeyToString(publicKey));

        logger.info("Key pair registered successfully");
        return keypair;
    }

    public static Vote createVote(PublicKey publicKey, PrivateKey privateKey, int voteValue){
        Vote newVote = new Vote(publicKey,voteValue);
        newVote.signVote(privateKey);
        logger.info("Vote created");
        return newVote;
    }

    public static void main(String[] args) throws Exception{
        logger.info("main started");


        KeyPair myKeys = register();
        PublicKey myPublicKey = myKeys.getPublic();
        PrivateKey myPrivateKey = myKeys.getPrivate();

        System.out.println("------------ Initialising Blockchain ------------\n");
        Blockchain blockchain = new Blockchain();
        System.out.println("\n");
        System.out.println(blockchain.getRemainingVoters());
        System.out.println("\n------------ Initialising Blockchain ------------\n");

        System.out.println("\n------------ Log Genesis Block ------------\n");
        System.out.println(ParserUtils.BlockToJSON(blockchain.getLastBlock()));
        System.out.println("\n------------ Log Genesis Block ------------\n");

        System.out.println("\n------------ Voting ------------\n");
        Vote myVote = createVote(myPublicKey,myPrivateKey,3);
        blockchain.addNewVote(myVote);
        System.out.println("\n------------ Voting ------------\n");

        System.out.println("\n------------ Blockchain Mining Vote ------------\n");
        blockchain.createNewBlock();
        System.out.println("\n------------ Blockchain Mining Vote ------------\n");




    }
}