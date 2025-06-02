package Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static SecretKey generateKey(String decryptionKey) {
        String password = decryptionKey;
        int keyLength = password.length();
        if (keyLength < 16) {
            password = String.format("%-16s", password).replace(' ', '0');
        } else if (keyLength < 24) {
            password = String.format("%-24s", password).replace(' ', '0');
        } else if (keyLength < 32) {
            password = String.format("%-32s", password).replace(' ', '0');
        } else if (keyLength > 32) {
            password = password.substring(0, 32);
        }

        byte[] keyBytes = password.getBytes();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        return secretKey;
    }
    public static byte[] encrypt(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data.getBytes());
    }

    public static String decrypt(byte[] encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes);
    }
}
