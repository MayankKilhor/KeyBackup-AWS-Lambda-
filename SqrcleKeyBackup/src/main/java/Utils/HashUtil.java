package Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {


    /**
     * Generates a hash by concatenating two input strings and applying the SHA-256 algorithm.
     *
     * @param input1 The first input string.
     * @param input2 The second input string.
     * @return The SHA-256 hash as a hexadecimal string.
     * @throws RuntimeException If an error occurs during hash generation.
     */
    public static String generateHash(String input1, String input2) {
        try {
            String input = input1 + input2; // Concatenate the two inputs

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

}
