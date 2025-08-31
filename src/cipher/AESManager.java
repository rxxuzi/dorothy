package cipher;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESManager {
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private SecretKey aesKey;
    private byte[] iv;
    
    public AESManager() {
        generateNewKey();
    }
    
    public AESManager(byte[] keyBytes) {
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
    }
    
    public void generateNewKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE);
            this.aesKey = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }
    
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            
            iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);
            
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH];
            
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);
            
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
            
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
    
    public byte[] getKeyBytes() {
        return aesKey.getEncoded();
    }
    
    public String getKeyBase64() {
        return Base64.getEncoder().encodeToString(getKeyBytes());
    }
    
    public static AESManager fromKeyBase64(String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new AESManager(keyBytes);
    }
}