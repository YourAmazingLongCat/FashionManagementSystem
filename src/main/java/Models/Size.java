package Models;

public class Size {

    private String sizeId;
    private String sizeName;
    private String categoryId;
    private String categoryName;

    public Size() {
    }

    public Size(String sizeId, String sizeName, String categoryId) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.categoryId = categoryId;
    }

    public String getSizeId() {
        return sizeId;
    }

    public void setSizeId(String sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
