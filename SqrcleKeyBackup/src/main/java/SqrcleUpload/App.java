//package SqrcleUpload;
//
//import Models.Circle;
//import Models.KeyBackup;
//import Utils.SecurityUtil;
//import Utils.ValidatorUtil;
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import request.KeyUploadRequest;
//import request.KeyUploadRequestParser;
//
//import javax.crypto.SecretKey;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
//
//    private final String bucketName = System.getenv("bucketName");
//    private final String region = System.getenv("regionName");
//    private final AmazonS3 s3Client = AmazonS3ClientBuilder
//            .standard()
//            .withRegion(region)
//            .build();
//
//    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
//        // Initialize logger
//        LambdaLogger logger = context.getLogger();
//        logger.log("Lambda function invoked with input: " + input.toString());
//
//        try {
//            String username = input.getHeaders().get("username");
//            String userId = input.getHeaders().get("userid");
//            String filePath = "/tmp/keys.json";
//            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
//            String requestBody = input.getBody();
//
//            logger.log("Parsing request body...");
//            KeyUploadRequest keyUploadRequest = KeyUploadRequestParser.parseRequest(requestBody);
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            if (userId == null || userId.length() != 16) {
//                logger.log("Invalid user ID: " + userId);
//                return response.withBody("UserId format is Invalid!").withStatusCode(400);
//            }
//
//            if (keyUploadRequest.getPrivateKey() == null) {
//                logger.log("Private key is missing.");
//                return response.withBody("PrivateKey can't be null!").withStatusCode(400);
//            }
//
//            String encryptionKey = keyUploadRequest.getPassword();
//            SecurityUtil securityUtil = new SecurityUtil();
//            SecretKey secretKey = securityUtil.generateKey(encryptionKey);
//
//            List<Circle> circles = keyUploadRequest.getCircles();
//            if (circles == null) {
//                logger.log("Circles list is null.");
//                return response.withBody("Circles list can't be null!").withStatusCode(400);
//            }
//
//            for (Circle circle : circles) {
//                if (circle == null || circle.getCircleId() == null || circle.getCircleHash() == null) {
//                    logger.log("Invalid circle details: " + circle);
//                    return response.withBody("Circle details are invalid!").withStatusCode(400);
//                }
//            }
//
//            logger.log("Validating circle and user ID...");
//            boolean validDetails = ValidatorUtil.validCircleandUserID(userId, circles);
//            if (!validDetails) {
//                logger.log("Validation failed for user ID and circles.");
//                return response.withBody("Incorrect circle details or userID").withStatusCode(400);
//            }
//
//            KeyBackup keyBackup = new KeyBackup(keyUploadRequest.getPrivateKey(), circles, userId);
//            String json = objectMapper.writeValueAsString(keyBackup);
//            String key = username + "/keys.json";
//
//            if (keyUploadRequest.getEncrypted()) {
//                logger.log("Encrypting data...");
//                byte[] encryptedData = securityUtil.encrypt(json, secretKey);
//
//                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
//                    outputStream.write(encryptedData);
//                } catch (IOException e) {
//                    logger.log("Error writing encrypted data to file: " + e.getMessage());
//                    return response.withBody("Error writing file: " + e.getMessage()).withStatusCode(500);
//                }
//            } else {
//                try (FileWriter fileWriter = new FileWriter(filePath)) {
//                    fileWriter.write(json);
//                } catch (IOException e) {
//                    logger.log("Error writing data to file: " + e.getMessage());
//                    return response.withBody("Error writing file: " + e.getMessage()).withStatusCode(500);
//                }
//            }
//
//            File file = new File(filePath);
//            logger.log("Uploading file to S3 bucket: " + bucketName + ", key: " + key);
//            s3Client.putObject(new PutObjectRequest(bucketName, key, file));
//
//            logger.log("Data successfully uploaded to S3.");
//            return response.withBody("Data is stored in: " + key).withStatusCode(200);
//
//        } catch (Exception e) {
//            logger.log("Exception occurred: " + e.getMessage());
//            return new APIGatewayProxyResponseEvent().withBody("Error: " + e.getMessage()).withStatusCode(500);
//        }
//    }
//}


package SqrcleUpload;

import Models.SqrcleCircle;
import Models.SqrcleKeyBackup;
import Utils.SecurityUtil;
import Utils.ValidatorUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import payload.request.KeySqrcleUploadRequest;
import payload.request.KeySqrcleUploadRequestParser;


import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final String bucketName = System.getenv("bucketName");
    private final String region = System.getenv("regionName");
    private final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(region)
            .build();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        // Initialize logger
        LambdaLogger logger = context.getLogger();
        logger.log("Lambda function invoked with input: " + input.toString());

        try {
            String username = input.getHeaders().get("username");
            String userId = input.getHeaders().get("userid");
            String filePath = "/tmp/keys.json";
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            String requestBody = input.getBody();

            logger.log("Parsing request body...");
            KeySqrcleUploadRequest keySqrcleUploadRequest = KeySqrcleUploadRequestParser.parseRequest(requestBody);
            ObjectMapper objectMapper = new ObjectMapper();

            if (userId == null || userId.length() != 16) {
                logger.log("Invalid user ID: " + userId);
                return response.withBody("UserId format is Invalid!").withStatusCode(400);
            }

            if (keySqrcleUploadRequest.getPrivateKey() == null) {
                logger.log("Private key is missing.");
                return response.withBody("PrivateKey can't be null!").withStatusCode(400);
            }

            String encryptionKey = keySqrcleUploadRequest.getPassword();
            SecurityUtil securityUtil = new SecurityUtil();
            SecretKey secretKey = securityUtil.generateKey(encryptionKey);

            List<SqrcleCircle> circles = keySqrcleUploadRequest.getCircles();
            if (circles == null) {
                logger.log("Circles list is null.");
                return response.withBody("Circles list can't be null!").withStatusCode(400);
            }

            for (SqrcleCircle circle : circles) {
                if (circle == null || circle.getCircleId() == null || circle.getCircleHash() == null) {
                    logger.log("Invalid circle details: " + circle);
                    return response.withBody("Circle details are invalid!").withStatusCode(400);
                }
            }

            logger.log("Validating circle and user ID...");
            boolean validDetails = ValidatorUtil.validSqrcleCircleandUserID(userId, circles);
            if (!validDetails) {
                logger.log("Validation failed for user ID and circles.");
                return response.withBody("Incorrect circle details or userID").withStatusCode(400);
            }

            SqrcleKeyBackup keyBackup = new SqrcleKeyBackup(keySqrcleUploadRequest.getPrivateKey(), circles, userId);
            String json = objectMapper.writeValueAsString(keyBackup);
            String key = username + "/keys.json";

            if (keySqrcleUploadRequest.getEncrypted()) {
                logger.log("Encrypting data...");
                byte[] encryptedData = securityUtil.encrypt(json, secretKey);

                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                    outputStream.write(encryptedData);
                } catch (IOException e) {
                    logger.log("Error writing encrypted data to file: " + e.getMessage());
                    return response.withBody("Error writing file: " + e.getMessage()).withStatusCode(500);
                }
            } else {
                try (FileWriter fileWriter = new FileWriter(filePath)) {
                    fileWriter.write(json);
                } catch (IOException e) {
                    logger.log("Error writing data to file: " + e.getMessage());
                    return response.withBody("Error writing file: " + e.getMessage()).withStatusCode(500);
                }
            }

            File file = new File(filePath);
            logger.log("Uploading file to S3 bucket: " + bucketName + ", key: " + key);
            s3Client.putObject(new PutObjectRequest(bucketName, key, file));

            logger.log("Data successfully uploaded to S3.");
            return response.withBody("Data is stored in: " + key).withStatusCode(200);

        } catch (Exception e) {
            logger.log("Exception occurred: " + e.getMessage());
            return new APIGatewayProxyResponseEvent().withBody("Error: " + e.getMessage()).withStatusCode(500);
        }
    }
}
