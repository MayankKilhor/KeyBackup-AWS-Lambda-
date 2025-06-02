package Models;


public class Circle {
    private String circleId;
    private String circleHash;
    private String circleColor;
    private Coi coi;

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getCircleHash() {
        return circleHash;
    }

    public void setCircleHash(String circleHash) {
        this.circleHash = circleHash;
    }

    public String getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(String circleColor) {
        this.circleColor = circleColor;
    }

    public Coi getCoi() {
        return coi;
    }

    public void setCoi(Coi coi) {
        this.coi = coi;
    }
}

