package app.resources.util;

import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.security.*; // KeyPairGenerator, KeyPair, PublicKey, PrivateKey, Signature, NoSuchAlgorithmException, KeyFactory
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;


public class Cryptography {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); // getInstance throws NoSuchAlgorithmException
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public static String sign(String data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance("SHA256withRSA"); // getInstance throws NoSuchAlgorithmException
        signer.initSign(privateKey); // initSign throws InvalidKeyException
        signer.update(data.getBytes()); // update throws SignatureException
        return Base64.getEncoder().encodeToString(signer.sign()); // sign throws SignatureException
    }

    public static boolean verify(String data, String signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature verifier = Signature.getInstance("SHA256withRSA"); // getInstance throws NoSuchAlgorithmException
        verifier.initVerify(publicKey); // initVerify throws InvalidKeyException
        verifier.update(data.getBytes()); // update throws SignatureException

        return verifier.verify(Base64.getDecoder().decode(signature));
    }

    public static String publicKeyToString(PublicKey publicKey){
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String privateKeyToString(PrivateKey privateKey){
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static PublicKey stringToPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // getInstance throws NoSuchAlgorithmException
            return keyFactory.generatePublic(spec); // generatePublic throws InvalidKeySpecException
    }

    public static PrivateKey stringToPrivateKey(String privateKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
    }

}