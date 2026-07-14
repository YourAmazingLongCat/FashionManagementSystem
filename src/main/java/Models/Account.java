package Models;

/**
 * Account Model for Fashion Management System
 * @author ADMIN
 */
public class Account {
    
    // 1. Khai báo các thuộc tính (Trường dữ liệu)
    private String accountId; 
    private String email;
    private String password;
    private String fullName;
    private String role; 
    private String status; 
    
    // ĐÃ THÊM: Các thuộc tính mới cho chức năng Profile
    private String avatar; 
    private String phone;  

    // 2. Hàm khởi tạo rỗng (Bắt buộc phải có trong Java Bean)
    public Account() {
    }

    // 3. Hàm khởi tạo đầy đủ tham số (Đã cập nhật thêm avatar và phone)
    public Account(String accountId, String email, String password, String fullName, String role, String status, String avatar, String phone) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.avatar = avatar;
        this.phone = phone;
    }

    // 4. Các hàm Getter và Setter
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // GETTER & SETTER CHO AVATAR VÀ PHONE
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 5. Hàm toString (Hỗ trợ in ra console để debug lỗi)
    @Override
    public String toString() {
        return "Account{" + "accountId=" + accountId + ", email=" + email + ", fullName=" + fullName + ", role=" + role + ", status=" + status + ", avatar=" + avatar + ", phone=" + phone + '}';
    }
}