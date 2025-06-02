package Download;

import Models.User;
import Utils.DatabaseUtil;
import Utils.SecurityUtil;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        try{
        String username ="Mayank@123";
        String objectKey = username+"/temp/keys.json";
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        Boolean encryption= true;


        String password ="Mayank@123";
//        KeyDownloadRequest keyDownloadRequest = KeyDownloadRequestParser.parseRequest(requestBody);
        User user = new User();
        DatabaseUtil databaseUtil = new DatabaseUtil();
        user = databaseUtil.findByUserName(username);
        if(user == null){
            System.out.println("500:- "+"Unable to Fetch User Details");

        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("500:- "+"Validation failed!");

        }
        String decryptionKey = password;
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
        String json="";
        if(encryption){
            SecurityUtil securityUtil = new SecurityUtil();
            SecretKey secretKey = securityUtil.generateKey(decryptionKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//            PKCS5Paddingbyte[] keyBytes = decryptionKey.getBytes();
//            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] encryptedData = s3Object.getObjectContent().readAllBytes();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);


            byte[] decryptedData = cipher.doFinal(encryptedData);
            json = new String(decryptedData);
        }else{
            // Read the content of the object
            BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            // Close the reader
            reader.close();
            json=content.toString();
        }


        System.out.println("200:- "+json);

    } catch(IOException e) {
        System.out.println("500:- "+"\n"+e.getMessage());


    }catch (Exception e){
        System.out.println("500:- "+"body+"+"\n"+e.getMessage());
    }
    }
}
