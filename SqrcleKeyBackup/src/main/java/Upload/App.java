package Upload;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Circle;
import Models.KeyBackup;
import Models.User;
import Utils.DatabaseUtil;
import Utils.SecurityUtil;
import Utils.ValidatorUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import payload.request.KeyUploadRequest;
import payload.request.KeyUploadRequestParser;
import payload.response.ApiResponse;
import Exception.ServiceUnavailableException;

import javax.crypto.SecretKey;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final String bucketName = System.getenv("bucketName");
    private final String region = System.getenv("regionName");
    private final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(region)
            .build();
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        logger.info("Lambda function invoked");
        try {
           String username =input.getHeaders().get("username");
           String userId =input.getHeaders().get("userid");
           logger.info("Received request for user: {}", username);
           logger.debug("UserID: {}", userId);

           String filePath = "/tmp/keys.json";
           APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
           String requestBody = input.getBody();
            logger.debug("Request body: {}", requestBody);

           KeyUploadRequest keyUploadRequest = KeyUploadRequestParser.parseRequest(requestBody);
           ObjectMapper objectMapper = new ObjectMapper();
           User user = new User();
           DatabaseUtil databaseUtil = new DatabaseUtil();
           user = databaseUtil.findByUserName(username);
           logger.info("User retrieved from database: {}", user != null);

            if (user == null) {
                return createErrorResponse(response, "Unable to fetch User details from the database!", username +" has no entry in database", 400);
            }
//           if(user == null){
//               ApiResponse apiResponse = new ApiResponse(false,"Unable to fetch User details");
//               apiResponse.addDetail("error","Unable to fetch User details from the database!");
//               apiResponse.addDetail("username",username);
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//           }
           PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (!passwordEncoder.matches(keyUploadRequest.getPassword(), user.getPassword())) {
                return createErrorResponse(response, "Authentication failed, Password is incorrect!", "For username: "+username +" , Password is incorrect", 401);
            }
//           if (!passwordEncoder.matches(keyUploadRequest.getPassword(), user.getPassword())) {
//               ApiResponse apiResponse = new ApiResponse(false,"Authentication failed, Password is incorrect");
//               apiResponse.addDetail("error","Authentication failed, Password is incorrect!");
//               apiResponse.addDetail("username",username);
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(401);
//           }
            if (userId == null) {
                return createErrorResponse(response, "UserId can't be null!", "UserId can't be null!", 400);
            }
            if (userId.length() != 16) {
                return createErrorResponse(response, "Invalid UserId format!", "Inavlid format for "+userId, 400);
            }
//           if(userId == null){
//               ApiResponse apiResponse = new ApiResponse(false,"UserId can't be null!");
//               apiResponse.addDetail("error","UserId can't be null!");
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//           }
//           if(userId.length()!=16){
//               ApiResponse apiResponse = new ApiResponse(false,"UserId format is Invalid!");
//               apiResponse.addDetail("error","UserId format is Invalid!");
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//           }
            if (keyUploadRequest.getPrivateValue() == null) {
                return createErrorResponse(response, "Private Key can't be null", "Private Key can't be null", 400);
            }
//           if(keyUploadRequest.getPrivateKey() == null){
//               ApiResponse apiResponse = new ApiResponse(false,"Private Key can't be null");
//               apiResponse.addDetail("error","Private Key can't be null!");
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//           }
           String encryptionKey = keyUploadRequest.getPassword();
           SecurityUtil securityUtil = new SecurityUtil();
           SecretKey secretKey = securityUtil.generateKey(encryptionKey);
            logger.info("Generated secret key for encryption");
            List<Circle> circles = keyUploadRequest.getCircles();
            if (circles == null || circles.isEmpty()) {
                return createErrorResponse(response, "Circle List can't be null or empty", "Circle List can't be null or empty!", 400);
            }

            for (Circle circle : circles) {
                if (circle == null || circle.getCircleId() == null || circle.getCircleHash() == null) {
                    return createErrorResponse(response, "Invalid circle details", "Invalid circle details!", 400);
                }
            }
//           if (circles == null) {
//               ApiResponse apiResponse = new ApiResponse(false,"Circle List can't be null");
//               apiResponse.addDetail("error","Circle List can't be null!");
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//
//           }
//           for (Circle circle : circles) {
//               if (circle == null) {
//                   ApiResponse apiResponse = new ApiResponse(false,"Circle can't be null");
//                   apiResponse.addDetail("error","Circle can't be null!");
//                   String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                   return response
//                           .withBody(jsonResponse)
//                           .withStatusCode(400);
//               }
//               if (circle.getCircleId() == null) {
//                   ApiResponse apiResponse = new ApiResponse(false,"Circle ID can't be null");
//                   apiResponse.addDetail("error","Circle ID can't be null!");
//                   String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                   return response
//                           .withBody(jsonResponse)
//                           .withStatusCode(400);
//               }
//               if (circle.getCircleHash() == null) {
//                   ApiResponse apiResponse = new ApiResponse(false,"Circle hash can't be null");
//                   apiResponse.addDetail("error","Circle hash can't be null!");
//                   String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                   return response
//                           .withBody(jsonResponse)
//                           .withStatusCode(400);
//               }
////               if(circle.getCoi() == null){
////                   return response
////                           .withBody("Circle COI cannot be null")
////                           .withStatusCode(400);
////               }
//           }
           Boolean validDetails = ValidatorUtil.validCircleandUserID(userId,circles);
            if (!validDetails) {
                return createErrorResponse(response, "Incorrect circle details or UserId", "Incorrect circle details or UserId!", 400);
            }
//           if(!validDetails){
//               ApiResponse apiResponse = new ApiResponse(false,"Incorrect circle details or UserId");
//               apiResponse.addDetail("error","Incorrect circle details or UserId!");
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//           }

//           if(keyUploadRequest.getCircles().get(0).getCircleId() == null ||keyUploadRequest.getCircles().get(0).getCircleHash()== null){
//               ApiResponse apiResponse = new ApiResponse(false,"Circle details Invalid");
//               apiResponse.addDetail("error","Circle details Invalid!");
//               String jsonResponse = objectMapper.writeValueAsString(apiResponse);
//               return response
//                       .withBody(jsonResponse)
//                       .withStatusCode(400);
//           }
           KeyBackup keyBackup = new KeyBackup(keyUploadRequest.getPrivateValue(),keyUploadRequest.getCircles(),userId);
           String json = objectMapper.writeValueAsString(keyBackup);
           String key = username+"/keys.json";
            logger.info("KeyBackup object created and serialized");
            if (keyUploadRequest.getEncrypted()) {
                byte[] encryptedData = securityUtil.encrypt(json, secretKey);
                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                    outputStream.write(encryptedData);
                    logger.info("Encrypted data written to file: {}", filePath);
                } catch (IOException e) {
                    logger.error("Failed to write encrypted data to file", e);
                    return createErrorResponse(response, "Failed to write encrypted data to file",e.getMessage(), 500);
                }
            } else {
                try (FileWriter fileWriter = new FileWriter(filePath)) {
                    fileWriter.write(json);
                    logger.info("Unencrypted data written to file: {}", filePath);
                } catch (IOException e) {
                    logger.error("Failed to write unencrypted data to file", e);
                    return createErrorResponse(response, "Failed to write unencrypted data to file",e.getMessage(), 500);
                }
            }
//           if(keyUploadRequest.getEncrypted()){
//               byte[] encryptedData = securityUtil.encrypt(json,secretKey);
////
//               try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
//                   outputStream.write(encryptedData);
//               } catch (IOException e) {
//                   ApiResponse apiResponse = new ApiResponse(false,"Unable to Upload Key");
//                   apiResponse.addDetail("error",e.getMessage());
//                   apiResponse.addDetail("body",input.getBody());
//                   String jsonResponse = null;
//                   try {
//                       jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                   } catch (JsonProcessingException ex) {
//                       throw new RuntimeException(ex);
//                   }
//                   return response
//                           .withBody(jsonResponse)
//                           .withStatusCode(500);
//               }
//           }else{
//               try (FileWriter fileWriter = new FileWriter(filePath)) {
//                   fileWriter.write(json);
//               } catch (IOException e) {
//                   ApiResponse apiResponse = new ApiResponse(false,"Unable to backup Key");
//                   apiResponse.addDetail("error",e.getMessage());
//                   apiResponse.addDetail("body",input.getBody());
//                   String jsonResponse = null;
//                   try {
//                       jsonResponse = objectMapper.writeValueAsString(apiResponse);
//                   } catch (JsonProcessingException ex) {
//                       throw new RuntimeException(ex);
//                   }
//                   return response
//                           .withBody(jsonResponse)
//                           .withStatusCode(500);
//               }
//           }

           File file = new File(filePath);
           s3Client.putObject(new PutObjectRequest(bucketName, key,file));
           logger.info("File uploaded to S3 bucket: {}", bucketName);

            try {
                Map<String, Object> fieldsToUpdate = new HashMap<>();
                fieldsToUpdate.put("keysBackup", true);
                fieldsToUpdate.put("backupEncryption", keyUploadRequest.getEncrypted());
                databaseUtil.updateFields(username, fieldsToUpdate);
            } catch (ServiceUnavailableException e) {
                return createErrorResponse(response, "Database service unavailable for backup update", e.getMessage(),503);
            }

            logger.info("Database fields updated for user: {}", username);


            ApiResponse apiResponse = new ApiResponse(true,"Successfully backedup the keys");
           apiResponse.addDetail("keyPath",key);
           String jsonResponse = objectMapper.writeValueAsString(apiResponse);
           return response
                   .withBody(jsonResponse)
                   .withStatusCode(200);

       }catch(Exception e) {
//           APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
//           ApiResponse apiResponse = new ApiResponse(false,"Unable to backup Key");
//           apiResponse.addDetail("error",e.getMessage());
//           apiResponse.addDetail("body",input.getBody());
//           ObjectMapper objectMapper = new ObjectMapper();
//           String jsonResponse = null;
//           try {
//               jsonResponse = objectMapper.writeValueAsString(apiResponse);
//           } catch (JsonProcessingException ex) {
//               jsonResponse = "{\"error\":\""+e.getMessage()+"\"}";
//           }
//           return response
//                   .withBody(jsonResponse)
//                   .withStatusCode(500);
//       }
            logger.error("Exception encountered while processing request", e);
            return createErrorResponse(new APIGatewayProxyResponseEvent(), e.getMessage(), input.getBody(), 500);
        }
    }

//    private APIGatewayProxyResponseEvent createErrorResponse(APIGatewayProxyResponseEvent response, String message, int statusCode, ObjectMapper objectMapper) {
//        return createErrorResponse(response, message, statusCode, objectMapper, null, null);
//    }
//
//    private APIGatewayProxyResponseEvent createErrorResponse(APIGatewayProxyResponseEvent response, String message, int statusCode, ObjectMapper objectMapper, Exception e, String requestBody) {
//        ApiResponse apiResponse = new ApiResponse(false, message);
//        if (e != null) {
//            apiResponse.addDetail("error", e.getMessage());
//        }
//        if (requestBody != null) {
//            apiResponse.addDetail("body", requestBody);
//        }
//        String jsonResponse;
//        try {
//            jsonResponse = objectMapper.writeValueAsString(apiResponse);
//        } catch (JsonProcessingException ex) {
//            jsonResponse = "{\"error\":\"" + message + "\"}";
//        }
//        return response.withBody(jsonResponse).withStatusCode(statusCode);
//    }
    private APIGatewayProxyResponseEvent createErrorResponse(APIGatewayProxyResponseEvent response, String error, String detail, int statusCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponse apiResponse = new ApiResponse(false, error);
        apiResponse.addDetail("error", detail);
//        if (detail != null) {
//            apiResponse.addDetail("detail", detail);
//        }
        try {
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            logger.error("Error response created: {} - {}", error, detail);
            return response.withBody(jsonResponse).withStatusCode(statusCode);
        } catch (JsonProcessingException ex) {
            logger.error("Error serializing JSON response", ex);
            return response.withBody("{\"error\":\"JSON processing error\"}").withStatusCode(500);
        }
    }
}
