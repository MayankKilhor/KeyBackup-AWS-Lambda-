//package Utils;
//
//import Models.User;
//import com.mongodb.ConnectionString;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import org.bson.Document;
//
//import java.util.Map;
//
//public class DatabaseUtil {
//    private static final String ServerMONGO_URI = "mongodb://admin:admin@ec2-157-175-139-21.me-south-1.compute.amazonaws.com:27017/sqircle_db";
//    private static final String LocalMONGO_URI ="mongodb://localhost:27017/sqircle_db";
//    private static final MongoClient mongoClient = MongoClients.create(new ConnectionString(ServerMONGO_URI));
////    private static final MongoClient mongoClient = MongoClients.create(new ConnectionString(LocalMONGO_URI));
//
//    public static User findByUserName(String userName) {
//        try {
//
//            MongoDatabase database = mongoClient.getDatabase("sqircle_db");
//
//            MongoCollection<Document> collection = database.getCollection("User");
//            Document query = new Document("userName", userName);
//            Document document = collection.find(query).first();
//            if(document != null){
//                User user = new User();
//                user.setValuesFromDocument(document);
//
//                System.out.println("Successfully fetched User details from the database");
//                return user;
//            }else{
//                System.out.println("Not able to fetch the details from database, recheck the values");
//                return null;
//            }
//
//        } catch (Exception e) {
//
//            System.out.println("Error :-"+e.getMessage());
//        }
//        return null;
//    }
//    public static void updateBackup(String userName,Boolean encryption) {
//        try {
//
//            MongoDatabase database = mongoClient.getDatabase("sqircle_db");
//
//            MongoCollection<Document> collection = database.getCollection("User");
//            Document query = new Document("userName", userName);
//            Document document = collection.find(query).first();
//            if(document != null){
//                document.put("backup", true);
//
//                // Replace the existing document with the updated one
//                collection.replaceOne(query, document);
//
//                System.out.println("Successfully fetched User details from the database");
//
//            }else{
//                System.out.println("Not able to fetch the details from database, recheck the values");
//
//            }
//
//        } catch (Exception e) {
//
//            System.out.println("Error :-"+e.getMessage());
//        }
//    }
//
//    public static void updateFields(String userName, Map<String, Object> fieldsToUpdate) {
//        try {
//            MongoDatabase database = mongoClient.getDatabase("sqircle_db");
//            MongoCollection<Document> collection = database.getCollection("User");
//
//            // Create a query to find the document to update
//            Document query = new Document("userName", userName);
//
//            // Create an update operation to set the values of the specified fields
//            Document updateOperation = new Document("$set", new Document(fieldsToUpdate));
//
//            // Update the document in the collection
//            collection.updateOne(query, updateOperation);
//
//            System.out.println("Fields updated successfully");
//        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
//        }
//    }
//}\

package Utils;

import Models.User;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Exception.ServiceUnavailableException;
import java.util.Map;

public class DatabaseUtil {
    private static final String ServerMONGO_URI = System.getenv("mongoURI");
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String LocalMONGO_URI = "mongodb://localhost:27017/sqircle_db";
    private static  MongoClient mongoClient = null;
    // private static final MongoClient mongoClient = MongoClients.create(new ConnectionString(LocalMONGO_URI));

    private static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            logger.info("Initializing MongoDB client...");
            mongoClient = MongoClients.create(new ConnectionString(ServerMONGO_URI));
        }
        return mongoClient;
    }

    private static synchronized void closeMongoClient() {
        if (mongoClient != null) {
            logger.info("Closing MongoDB client...");
            mongoClient.close();
            mongoClient = null;
        }
    }

    public static MongoDatabase getDatabase(String dbName) {
        return getMongoClient().getDatabase(dbName);
    }

    public static User findByUserName(String userName) throws Exception {
        try {

            MongoDatabase database = getDatabase("Devsqrcle");
            MongoCollection<Document> collection = database.getCollection("User");

            Document query = new Document("userName", userName);
            Document document = collection.find(query).first();

            if (document != null) {
                User user = new User();
                user.setValuesFromDocument(document);

                logger.info("Successfully fetched User details from the database for user: {}" + userName);
                return user;
            } else {
                logger.info("No user details found in the database for user: {}"+  userName);
                throw  new Exception("No user details found in the database for user: {}"+  userName);
            }
        } catch (Exception e) {
            logger.error("Error fetching user details from database for user: {}"+userName+ ", error: {}"+e.getMessage());
            throw e;
        }
    }

    public static void updateBackup(String userName, Boolean encryption) {
        try {
            MongoDatabase database = getDatabase("Devsqrcle");
            MongoCollection<Document> collection = database.getCollection("User");

            Document query = new Document("userName", userName);
            Document document = collection.find(query).first();

            if (document != null) {
                document.put("backup", true);
                document.put("encryption", encryption);

                collection.replaceOne(query, document);

                logger.info("Successfully updated backup details for user: {}", userName);
            } else {
                logger.warn("No user found in the database to update backup for user: {}", userName);
            }
        } catch (Exception e) {
            logger.error("Error updating backup details for user: {}, error: {}", userName, e.getMessage());
        }
    }

    public static void updateFields(String userName, Map<String, Object> fieldsToUpdate) throws ServiceUnavailableException {
        try {
            MongoDatabase database = getDatabase("Devsqrcle");
            MongoCollection<Document> collection = database.getCollection("User");

            Document query = new Document("userName", userName);
            Document updateOperation = new Document("$set", new Document(fieldsToUpdate));

            collection.updateOne(query, updateOperation);

            logger.info("Fields updated successfully for user: {}", userName);
        } catch (Exception e) {
            logger.error("Error updating fields for user: {}, error: {}", userName, e.getMessage());
            throw new ServiceUnavailableException("Service unavailable: Unable to update backup details, Error:- "+e.getMessage());
        }
    }

    public static void cleanUp() {
        closeMongoClient();
    }
}

