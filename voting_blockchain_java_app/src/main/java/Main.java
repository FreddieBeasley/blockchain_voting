import blockchain.*;

import util.CryptographyUtils;
import util.ParserUtils;
import util.FileHandlingUtils;

import java.util.ArrayList;
import java.util.List;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static KeyPair register() throws Exception{
        KeyPair keypair;

        try {
            keypair = CryptographyUtils.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Unable to generate keypair", e);
        }

        PublicKey publicKey = keypair.getPublic();

        FileHandlingUtils.appendToJSONFileArray("src/main/data/registeredVoters.json", CryptographyUtils.publicKeyToString(publicKey));

        return keypair;
    }

    public static void main(String[] args) throws Exception {
        logger.info("main started");

        List<Vote> voteList = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            KeyPair myKeys = register();
            logger.info("new voter registered with public key: " + CryptographyUtils.publicKeyToString(myKeys.getPublic()));

            Vote vote = new Vote(CryptographyUtils.publicKeyToString(myKeys.getPublic()), i);
            vote.signVote(myKeys.getPrivate());
            voteList.add(vote);
        }

        logger.info("voter registration finished");

        // loads blockchain, add 1 vote, save blockchain
        Blockchain blockchain1 = new Blockchain();

        blockchain1.addNewVote(voteList.get(1));

        blockchain1.createNewBlock();

        // reload blockchain, add 2 votes, save blockchain
        Blockchain blockchain2 = new Blockchain();

        blockchain2.addNewVote(voteList.get(2));
        blockchain2.addNewVote(voteList.get(3));

        blockchain2.createNewBlock();

        // reload blockchain, add 3 votes, save blockchain
        Blockchain blockchain3 = new Blockchain();

        blockchain3.addNewVote(voteList.get(4));
        blockchain3.addNewVote(voteList.get(5));

        blockchain3.createNewBlock();
    }
}