package cipher;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import java.util.*;

public class RSAManager {
    private static final String PUBLIC_KEY_FILE = "doro_pub.pem";
    private static final String PRIVATE_KEY_FILE = "doro_prv.pem";
    private static final String ENC_PREFIX = "ENC:";
    private static final int KEY_SIZE = 2048;

    private KeyPair keyPair;

    public RSAManager() {
        loadOrGenerateKeys();
    }

    public void loadOrGenerateKeys() {
        try {
            File publicKeyFile = new File(PUBLIC_KEY_FILE);
            File privateKeyFile = new File(PRIVATE_KEY_FILE);

            if (publicKeyFile.exists() && privateKeyFile.exists()) {
                loadKeys(publicKeyFile, privateKeyFile);
            } else {
                generateNewKeys();
            }
        } catch (Exception e) {
            System.err.println("Error with keys: " + e.getMessage());
            generateNewKeys();
        }
    }

    private void loadKeys(File publicKeyFile, File privateKeyFile) throws Exception {
        // Load public key from PEM
        String publicPem = Files.readString(publicKeyFile.toPath());
        byte[] publicKeyBytes = pemToBytes(publicPem, "PUBLIC KEY");

        // Load private key from PEM
        String privatePem = Files.readString(privateKeyFile.toPath());
        byte[] privateKeyBytes = pemToBytes(privatePem, "PRIVATE KEY");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        PublicKey publicKey = keyFactory.generatePublic(publicSpec);
        PrivateKey privateKey = keyFactory.generatePrivate(privateSpec);

        keyPair = new KeyPair(publicKey, privateKey);
    }

    public void generateNewKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE);
            keyPair = keyGen.generateKeyPair();

            saveKeys();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate keys", e);
        }
    }

    private void saveKeys() throws IOException {
        // Save public key as PEM
        String publicPem = bytesToPem(keyPair.getPublic().getEncoded(), "PUBLIC KEY");
        Files.writeString(Paths.get(PUBLIC_KEY_FILE), publicPem);

        // Save private key as PEM
        String privatePem = bytesToPem(keyPair.getPrivate().getEncoded(), "PRIVATE KEY");
        Files.writeString(Paths.get(PRIVATE_KEY_FILE), privatePem);
    }

    public void exportPublicKey(File file) throws IOException {
        String publicPem = bytesToPem(keyPair.getPublic().getEncoded(), "PUBLIC KEY");
        Files.writeString(file.toPath(), publicPem);
    }

    public void importPublicKey(File file) throws Exception {
        String pem = Files.readString(file.toPath());
        byte[] keyBytes = pemToBytes(pem, "PUBLIC KEY");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        PublicKey publicKey = keyFactory.generatePublic(spec);

        // Keep existing private key but update public key
        keyPair = new KeyPair(publicKey, keyPair.getPrivate());
    }

    public String encrypt(String plainText) throws Exception {
        return encryptHybrid(plainText);
    }

    public String decrypt(String encryptedText) throws Exception {
        return decryptHybrid(encryptedText);
    }

    public boolean isEncrypted(String text) {
        return text != null && text.startsWith(ENC_PREFIX);
    }
    
    
    public String encryptAESKey(byte[] aesKey) throws Exception {
        if (aesKey.length > 245) {
            throw new IllegalArgumentException("AES key too long for RSA encryption");
        }
        
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        
        byte[] encrypted = cipher.doFinal(aesKey);
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public byte[] decryptAESKey(String encryptedKey) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(encryptedKey);
        
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        
        return cipher.doFinal(encrypted);
    }
    
    private String encryptHybrid(String plainText) throws Exception {
        AESManager aes = new AESManager();
        
        String encryptedData = aes.encrypt(plainText);
        
        String encryptedKey = encryptAESKey(aes.getKeyBytes());
        
        return ENC_PREFIX + encryptedKey + ":" + encryptedData;
    }
    
    private String decryptHybrid(String encryptedText) throws Exception {
        if (!encryptedText.startsWith(ENC_PREFIX)) {
            throw new IllegalArgumentException("Not an encrypted text");
        }
        
        String data = encryptedText.substring(ENC_PREFIX.length());
        
        int colonIndex = data.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException("Invalid encrypted format");
        }
        
        String encryptedKey = data.substring(0, colonIndex);
        String encryptedData = data.substring(colonIndex + 1);
        
        byte[] aesKey = decryptAESKey(encryptedKey);
        
        AESManager aes = new AESManager(aesKey);
        
        return aes.decrypt(encryptedData);
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public boolean hasValidKeys() {
        return keyPair != null && keyPair.getPublic() != null && keyPair.getPrivate() != null;
    }

    public String getPublicKeyFingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(keyPair.getPublic().getEncoded());
            return bytesToHex(digest).substring(0, 16); // Return first 16 chars
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // PEM format conversion utilities
    private String bytesToPem(byte[] keyBytes, String keyType) {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(keyType).append("-----\n");

        String base64 = Base64.getEncoder().encodeToString(keyBytes);

        // Split into 64-character lines (PEM standard)
        int index = 0;
        while (index < base64.length()) {
            int endIndex = Math.min(index + 64, base64.length());
            pem.append(base64, index, endIndex).append("\n");
            index = endIndex;
        }

        pem.append("-----END ").append(keyType).append("-----\n");
        return pem.toString();
    }

    private byte[] pemToBytes(String pem, String keyType) {
        // Remove header and footer
        String beginMarker = "-----BEGIN " + keyType + "-----";
        String endMarker = "-----END " + keyType + "-----";

        pem = pem.replace(beginMarker, "");
        pem = pem.replace(endMarker, "");
        pem = pem.replaceAll("\\s", ""); // Remove all whitespace

        return Base64.getDecoder().decode(pem);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}