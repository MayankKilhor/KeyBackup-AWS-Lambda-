package payload.request;

import Models.SqrcleCircle;

import java.util.List;

public class KeySqrcleUploadRequest {
    String privateKey;

    String password;

    private List<SqrcleCircle> circles;

    Boolean encrypted = false;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
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

    public List<SqrcleCircle> getCircles() {
        return circles;
    }

    public void setCircles(List<SqrcleCircle> circles) {
        this.circles = circles;
    }
}
