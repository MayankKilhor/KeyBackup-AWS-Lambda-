package Download;

import Models.User;
import Utils.DatabaseUtil;
import Utils.SecurityUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import payload.request.KeyDownloadRequest;
import payload.request.KeyDownloadRequestParser;
import payload.response.ApiResponse;

import javax.crypto.SecretKey;
import java.io.*;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private final String bucketName = System.getenv("bucketName");
    private final String region = System.getenv("regionName");
    private final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(region)
            .build();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        logger.info("Lambda function invoked with input: {}", input);

        String username = input.getHeaders().get("username");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            logger.debug("Extracting username from headers...");
            if (username == null || username.isEmpty()) {
                logger.error("Username is missing in the request headers");
                ApiResponse apiResponse = new ApiResponse(false, "Username is missing");
                apiResponse.addDetail("error", "Username is missing in the request headers");

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                return response.withBody(jsonResponse).withStatusCode(400);
            }

            username = username.toLowerCase();
            logger.info("Processed username: {}", username);

            String objectKey = username + "/keys.json";
            String requestBody = input.getBody();
            logger.debug("Parsing KeyDownloadRequest from body...");
            KeyDownloadRequest keyDownloadRequest = KeyDownloadRequestParser.parseRequest(requestBody);

            DatabaseUtil databaseUtil = new DatabaseUtil();
            logger.info("Fetching user details for username: {}", username);
            User user = new User();
            try {
                user = databaseUtil.findByUserName(username);
            }catch(Exception e){
                logger.warn("User not found in database: {}", username);
                ApiResponse apiResponse = new ApiResponse(false, "Unable to fetch User details");
                apiResponse.addDetail("error", "Username doesn't exist!");
                apiResponse.addDetail("errorDetailed",e.getMessage());
                apiResponse.addDetail("username", username);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                return response.withBody(jsonResponse).withStatusCode(400);
            }

            if (user == null) {
                logger.warn("User not found in database: {}", username);
                ApiResponse apiResponse = new ApiResponse(false, "Unable to fetch User details");
                apiResponse.addDetail("error", "Username doesn't exist!");
                apiResponse.addDetail("username", username);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                return response.withBody(jsonResponse).withStatusCode(400);
            }

            if (!user.getKeysBackup()) {
                logger.warn("Backup not available for user: {}", username);
                ApiResponse apiResponse = new ApiResponse(false, "User Backup doesn't exist");
                apiResponse.addDetail("error", "User Backup doesn't exist!");
                apiResponse.addDetail("username", username);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                return response.withBody(jsonResponse).withStatusCode(400);
            }

            logger.debug("Validating password for username: {}", username);
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (!passwordEncoder.matches(keyDownloadRequest.getPassword(), user.getPassword())) {
                logger.error("Authentication failed for username: {}", username);
                ApiResponse apiResponse = new ApiResponse(false, "Authentication failed, Password is incorrect");
                apiResponse.addDetail("error", "Authentication failed, Password is incorrect!");
                apiResponse.addDetail("username", username);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                return response.withBody(jsonResponse).withStatusCode(401);
            }

            String decryptionKey = keyDownloadRequest.getPassword();
            boolean isEncrypted = user.getBackupEncryption();
            logger.info("Fetching object from S3 for user: {}, Object Key: {}", username, objectKey);
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
            String json;

            if (isEncrypted) {
                logger.debug("Decrypting the backup for user: {}", username);
                byte[] encryptedData = s3Object.getObjectContent().readAllBytes();
                SecurityUtil securityUtil = new SecurityUtil();
                SecretKey secretKey = securityUtil.generateKey(decryptionKey);
                json = securityUtil.decrypt(encryptedData, secretKey);
            } else {
                logger.debug("Reading backup content for user: {}", username);
                BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                json = content.toString();
            }
            //NEW CODE
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            String newJson="";
            if (rootNode.has("privateValue")) {
                System.out.println("JSON contains privateValue, returning as is.");
                newJson = json;
            } else if (rootNode.has("privateKey")) {
                System.out.println("JSON contains privateKey, renaming it to privateValue.");
                ((ObjectNode) rootNode).put("privateValue", rootNode.get("privateKey").asText());
                ((ObjectNode) rootNode).remove("privateKey");
                newJson =  objectMapper.writeValueAsString(rootNode);
            }



            logger.debug("Parsing S3 content into response format...");
//            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = new ApiResponse(true, "Successfully downloaded key");
            //OLD ONE
//            Map<String, Object> keysMap = objectMapper.readValue(json, Map.class);
            //NEW ONE
            Map<String, Object> keysMap = objectMapper.readValue(newJson, Map.class);
            apiResponse.addDetail("keys", keysMap);

            logger.info("Successfully processed the request for username: {}", username);
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            return response.withBody(jsonResponse).withStatusCode(200);

        } catch (IOException e) {
            logger.error("IOException occurred while processing request: {}", e.getMessage(), e);
            return buildErrorResponse(response, "Unable to download Key", e, input);

        } catch (Exception e) {
            logger.error("Exception occurred while processing request: {}", e.getMessage(), e);
            return buildErrorResponse(response, "Unable to download Key", e, input);

        } finally {
            logger.info("Cleaning up database resources...");
            DatabaseUtil.cleanUp();
        }
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(
            APIGatewayProxyResponseEvent response,
            String message,
            Exception exception,
            APIGatewayProxyRequestEvent input) {
        ApiResponse apiResponse = new ApiResponse(false, message);
        apiResponse.addDetail("error", exception.getMessage());
        apiResponse.addDetail("body", input.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            return response.withBody(jsonResponse).withStatusCode(500);
        } catch (JsonProcessingException ex) {
            logger.error("Error serializing error response: {}", ex.getMessage(), ex);
            return response.withBody("{\"error\":\"Internal Server Error\"}").withStatusCode(500);
        }
    }
}



//package Download;
//
//import Models.User;
//import Utils.DatabaseUtil;
//import Utils.SecurityUtil;
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.GetObjectRequest;
//import com.amazonaws.services.s3.model.S3Object;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import payload.request.KeyDownloadRequest;
//import payload.request.KeyDownloadRequestParser;
//import payload.response.ApiResponse;
//
//import javax.crypto.SecretKey;
//import java.io.*;
//import java.util.Map;
//
//public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
//    private static final Logger logger = LoggerFactory.getLogger(App.class);
//    private final String bucketName = System.getenv("bucketName");
//    private final String region = System.getenv("regionName");
//    private final AmazonS3 s3Client = AmazonS3ClientBuilder
//            .standard()
//            .withRegion(region)
//            .build();
//
//
//    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
//
//        logger.info("Lambda function invoked with username: " + input.getHeaders().get("username"));
//        try {
//            String username =input.getHeaders().get("username");
//            username = username.toLowerCase();
//            String objectKey = username+"/keys.json";
//            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
//
//            String requestBody = input.getBody();
//            KeyDownloadRequest keyDownloadRequest = KeyDownloadRequestParser.parseRequest(requestBody);
//
//            if (username == null || username.isEmpty()) {
//                logger.error("Username is missing in the request headers");
//                ApiResponse apiResponse = new ApiResponse(false,"Username is missing");
//                apiResponse.addDetail("error","Username is missing in the request headers");
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                return response.withBody(jsonResponse).withStatusCode(400);
//            }
//            User user = new User();
//            DatabaseUtil databaseUtil = new DatabaseUtil();
//
//            user = databaseUtil.findByUserName(username);
//            logger.info("Found user in database of " + username+ " ,user:- "+user);
//            logger.info("Processed username: " + username);
////            context.getLogger().log("Processed username: " + username);
//            if(user == null){
//                ApiResponse apiResponse = new ApiResponse(false,"Unable to fetch User details");
//                apiResponse.addDetail("error","Username doesn't exist!");
//                apiResponse.addDetail("username",username);
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                return response
//                        .withBody(jsonResponse)
//                        .withStatusCode(400);
//            }
//
//            if(!user.getKeysBackup()){
//                ApiResponse apiResponse = new ApiResponse(false,"User Backup doesn't exist");
//                apiResponse.addDetail("error","User Backup doesn't exist!");
//                apiResponse.addDetail("username",username);
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                return response
//                        .withBody(jsonResponse)
//                        .withStatusCode(400);
//            }
//            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//            if (!passwordEncoder.matches(keyDownloadRequest.getPassword(), user.getPassword())) {
//                ApiResponse apiResponse = new ApiResponse(false,"Authentication failed, Password is incorrect");
//                apiResponse.addDetail("error","Authentication failed, Password is incorrect!");
//                apiResponse.addDetail("username",username);
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                return response
//                        .withBody(jsonResponse)
//                        .withStatusCode(401);
//            }
//            String decryptionKey = keyDownloadRequest.getPassword();
//            Boolean Encrypted = user.getBackupEncryption();
//
//            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
//            String json="";
//            if(Encrypted){
//                byte[] encryptedData = s3Object.getObjectContent().readAllBytes();
//                SecurityUtil securityUtil = new SecurityUtil();
//                SecretKey secretKey = securityUtil.generateKey(decryptionKey);
//                json = securityUtil.decrypt(encryptedData,secretKey);
//
//            }else{
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
//                StringBuilder content = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    content.append(line);
//                }
//
//                reader.close();
//                json=content.toString();
//            }
//
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            ApiResponse apiResponse = new ApiResponse(true,"Successfully downloaded key");
//            Map<String, Object> keysMap = objectMapper.readValue(json, Map.class);
//            apiResponse.addDetail("keys",keysMap);
//
//            logger.info("Successfully processed the request for username: " + username);
//            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//            return response
//                    .withBody(jsonResponse)
//                    .withStatusCode(200);
//
//        } catch (IOException e) {
//            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
//            logger.error("IOException occurred: " + e.getMessage());
//            ApiResponse apiResponse = new ApiResponse(false,"Unable to download Key");
//            apiResponse.addDetail("error",e.getMessage());
//            apiResponse.addDetail("body",input.getBody());
//            ObjectMapper objectMapper = new ObjectMapper();
//            String jsonResponse = null;
//            try {
//                jsonResponse = objectMapper.writeValueAsString(apiResponse);
//            } catch (JsonProcessingException ex) {
//                throw new RuntimeException(ex);
//            }
//            return response
//                    .withBody(jsonResponse)
//                    .withStatusCode(500);
//
//        }catch (Exception e){
//            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
//            logger.error("Exception occurred: " + e.getMessage());
//            ApiResponse apiResponse = new ApiResponse(false,"Unable to download Key");
//            apiResponse.addDetail("error",e.getMessage());
//            apiResponse.addDetail("body",input.getBody());
//            ObjectMapper objectMapper = new ObjectMapper();
//            String jsonResponse = null;
//            try {
//                jsonResponse = objectMapper.writeValueAsString(apiResponse);
//            } catch (JsonProcessingException ex) {
//                jsonResponse = "{\"error\":\""+e.getMessage()+"\"}";
//            }
//            return response
//                    .withBody(jsonResponse)
//                    .withStatusCode(500);
//        }finally {
//            // Clean up the MongoDB connection
//            DatabaseUtil.cleanUp();
//        }
//    }
//
//}
//
