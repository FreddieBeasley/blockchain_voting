package app.resources.util;

import app.resources.exceptions.InvalidException;

import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.security.*; // KeyPairGenerator, KeyPair, PublicKey, PrivateKey, Signature, NoSuchAlgorithmException, KeyFactory
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;


public class Cryptography {
    private static final String signatureAlgorithm = "SHA256withRSA";
    private static final String keyAlgorithm = "RSA";
    private static final int keySize = 2048;

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm); // getInstance throws NoSuchAlgorithmException
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    public static String sign(String data, String privateKeyStr) throws InvalidException {
        try {
            PrivateKey privateKey = stringToPrivateKey(privateKeyStr);
            Signature signer = Signature.getInstance(signatureAlgorithm); // getInstance throws NoSuchAlgorithmException
            signer.initSign(privateKey); // initSign throws InvalidKeyException
            signer.update(data.getBytes()); // update throws SignatureException
            return Base64.getEncoder().encodeToString(signer.sign()); // sign throws SignatureException
        } catch (Exception e) {
            throw new InvalidException(e.getMessage());
        }
    }

    public static boolean verify(String data, String signature, String publicKeyStr) throws InvalidException {
        try {
            PublicKey publicKey = stringToPublicKey(publicKeyStr);
            Signature verifier = Signature.getInstance(signatureAlgorithm); // getInstance throws NoSuchAlgorithmException
            verifier.initVerify(publicKey); // initVerify throws InvalidKeyException
            verifier.update(data.getBytes()); // update throws SignatureException

            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            throw new InvalidException(e.getMessage());
        }

    }

    public static String publicKeyToString(PublicKey publicKey){
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String privateKeyToString(PrivateKey privateKey){
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static PublicKey stringToPublicKey(String publicKeyString) throws InvalidException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm); // getInstance throws NoSuchAlgorithmException
            return keyFactory.generatePublic(spec); // generatePublic throws InvalidKeySpecException
        } catch (Exception e) {
            throw new InvalidException(e.getMessage());
        }
    }

    public static PrivateKey stringToPrivateKey(String privateKeyString) throws InvalidException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new InvalidException(e.getMessage());
        }
    }

    public static String hash(String data) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);

    }
}