package Models;

import java.util.List;

public class KeyBackup {

    private String privateValue;
    private List<Circle> circles;
    private String userId;

    public KeyBackup(String privateValue, List<Circle> circles, String userId) {
        this.privateValue = privateValue;
        this.circles = circles;
        this.userId = userId;
    }

    public String getPrivateValue() {
        return privateValue;
    }

    public void setPrivateValue(String privateValue) {
        this.privateValue = privateValue;
    }


    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
