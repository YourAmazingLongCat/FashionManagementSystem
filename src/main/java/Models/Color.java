package Models;

public class Color {

    private String colorId;
    private String colorName;
    private String hexCode;

    public Color() {
    }

    public Color(String colorId, String colorName, String hexCode) {
        this.colorId = colorId;
        this.colorName = colorName;
        this.hexCode = hexCode;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }
}
