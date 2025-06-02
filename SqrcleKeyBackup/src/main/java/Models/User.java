package Models;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User {
    private String username;

    private String password;

    private ObjectId id;

    private Boolean keysBackup;

    private Boolean backupEncryption;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Boolean getKeysBackup() {
        return keysBackup;
    }

    public void setKeysBackup(Boolean keysBackup) {
        this.keysBackup = keysBackup;
    }

    public Boolean getBackupEncryption() {
        return backupEncryption;
    }

    public void setBackupEncryption(Boolean backupEncryption) {
        this.backupEncryption = backupEncryption;
    }

    public void setValuesFromDocument(Document document) {
        this.password = document.getString("password");
        this.id = document.getObjectId("_id");
        this.username =document.getString("userName");
        this.keysBackup = document.getBoolean("keysBackup");
        this.backupEncryption=document.getBoolean("backupEncryption");

    }

}
