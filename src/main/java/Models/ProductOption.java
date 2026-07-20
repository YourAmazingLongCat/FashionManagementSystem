package Models;

/**
 * Model rút gọn (chỉ id + tên) dùng để đổ vào dropdown lọc sản phẩm
 * trên màn hình thống kê "Theo sản phẩm".
 */
public class ProductOption {

    private String productId;
    private String productName;

    public ProductOption() {
    }

    public ProductOption(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
