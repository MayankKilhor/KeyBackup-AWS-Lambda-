package Models;

import java.util.List;

public class SqrcleKeyBackup {

    private String privateValue;
    private List<SqrcleCircle> circles;
    private String userId;

    public SqrcleKeyBackup(String privateValue, List<SqrcleCircle> circles, String userId) {
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


    public List<SqrcleCircle> getCircles() {
        return circles;
    }

    public void setSCircles(List<SqrcleCircle> circles) {
        this.circles = circles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
