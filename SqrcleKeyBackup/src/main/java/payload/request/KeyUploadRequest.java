package payload.request;

import Models.Circle;

import java.util.List;

public class KeyUploadRequest {

    String privateValue;

    String password;

    private List<Circle> circles;

    Boolean encrypted = false;

    public String getPrivateValue() {
        return privateValue;
    }

    public void setPrivateValue(String privateValue) {
        this.privateValue = privateValue;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
    }
}



