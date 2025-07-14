package util;

import java.util.Base64;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;


public class CryptographyUtils {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); // Returns a KeyPairGenerator that generate RSA key pairs
        keyPairGenerator.initialize(2048);  // Initialises KeyPairGenerator for RSA 2048 ( as apose to 1024 )
        KeyPair keyPair = keyPairGenerator.generateKeyPair(); // Returns an RSA key pair
        return keyPair;
    }

    public static String sign(String data, PrivateKey privateKey) throws Exception{
        Signature signer = Signature.getInstance("SHA256withRSA"); // Signature object for SHA-256
        signer.initSign(privateKey); // Initialises object for signing ( with given private key )
        signer.update(data.getBytes()); // Updates data to be signed ( with vote data )

        String signature = Base64.getEncoder().encodeToString(signer.sign());

        return signature;
    }

    public static boolean verify(String data, String signature, PublicKey publicKey) throws Exception{
        Signature verifier = Signature.getInstance("SHA256withRSA"); // Signature object for SHA-256
        verifier.initVerify(publicKey); // Initialises the object for verification ( with expected signers public_key )
        verifier.update(data.getBytes()); // Updates data to be verified ( with expected vote data )

        return verifier.verify(Base64.getDecoder().decode(signature));
    }

}