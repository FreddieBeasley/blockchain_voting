import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.resources.util.Cryptography;
import app.resources.util.FileHandlers;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Scanner;
import java.io.File;

public class registration {
    private static final Logger logger = LoggerFactory.getLogger(registration.class);
    private static final File registerd_voters_file = new File("src/main/data/registered_voters.json");

    public static void main(String[] args) {
        logger.info("Starting registration process...");
        // CLI for this part
        while (true){
            Scanner in = new Scanner(System.in);
            logger.info(
                    "Please select an option:\n" +
                            "\t 1. Register to vote\n" +
                            "\t 2. End registration"
            );

            int choice = in.nextInt();
            if (choice == 1) {
                String publicKey;
                PrivateKey privateKey;
                String privateKeyStr;

                try {
                    KeyPair keyPair = Cryptography.generateKeyPair();
                    publicKey = Cryptography.publicKeyToString(keyPair.getPublic());
                    privateKey = keyPair.getPrivate();

                    privateKeyStr = Cryptography.privateKeyToString(keyPair.getPrivate());

                    // Debugging:
                    byte[] privateBytes = privateKey.getEncoded();

                } catch (Exception e) {
                    logger.error("Unable to generate keys: " + e.getMessage());
                    continue;
                }
                try {
                    FileHandlers.appendToJSONFileArray(registerd_voters_file.getPath(),publicKey);
                    logger.info("Successfully registered voters!");
                } catch (Exception e) {
                    logger.error("Unable to register voters: " + e.getMessage());
                    logger.error("CWD:" + System.getProperty("user.dir"));
                    continue;
                }

                logger.info("\n\n" +
                        "Public Key:\n" + publicKey + "\n" +
                                "Private Key:\n" + privateKeyStr + "\n"
                        );
            } else {
                break;
            }



        }
    }

}