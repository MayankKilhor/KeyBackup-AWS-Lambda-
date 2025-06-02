package Upload;

import Models.User;
import Utils.DatabaseUtil;
import Utils.SecurityUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Local {

    private static final String bucketName = "testingkeybackup";
    private static final String region = "me-south-1";

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    private static final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(region)
            .build();

    public static void main(String[] args){

        try {
            File tempFile = null;
            try {
                tempFile = File.createTempFile("keys", ".json");
            } catch (IOException e) {
                System.out.println("500:- " + e.getMessage());
            }

            String filePath = tempFile.getAbsolutePath();
            String username ="Mayank@123";
//            String filePath = "/tmp/keys.json";
            String password ="Mayank@123";
            String privateKey = "ThisisPrivateKey";
            Boolean encrypted =true;
            ObjectMapper objectMapper = new ObjectMapper();
            User user = new User();
            DatabaseUtil databaseUtil = new DatabaseUtil();
            user = databaseUtil.findByUserName(username);
            if(user == null){
                System.out.println("500:- "+"Unable to Fetch User Details");
            }
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("500:- "+"Validation Failed!");
            }
            String encryptionKey = password;
            SecurityUtil securityUtil = new SecurityUtil();
            SecretKey secretKey = securityUtil.generateKey(encryptionKey);
//            KeyBackup keyBackup = new KeyBackup(privateKey);
            String json = objectMapper.writeValueAsString(password);
            String key = username+"/temp/keys.json";
            Map<String, Object> fieldsToUpdate = new HashMap<>();
            fieldsToUpdate.put("walletId","WALLET_6643055bff91291cf7cb1a95");
            fieldsToUpdate.put("keysBackup", false);
            fieldsToUpdate.put("backupEncryption", false);
            databaseUtil.updateFields("Mayank@123", fieldsToUpdate);
//            if(encrypted){
//                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
////                byte[] keyBytes = encryptionKey.getBytes();
////                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
//                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//                byte[] encryptedData = cipher.doFinal(json.getBytes());
//                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
//                    outputStream.write(encryptedData);
//                } catch (IOException e) {
//                    System.out.println("500:- "+e.getMessage());
//
//
//                }
//            }else{
//                try (FileWriter fileWriter = new FileWriter(filePath)) {
//                    fileWriter.write(json);
//                } catch (IOException e) {
//                    System.out.println("500:- "+e.getMessage());
//                }
//            }
//
//            File file = new File(filePath);
//            s3Client.putObject(new PutObjectRequest(bucketName, key,file));
            System.out.println("200:- "+"Data is stored in :-"+key);


        }catch(Exception e){

            System.out.println("500:- "+e.getMessage());


        }
    }
}

