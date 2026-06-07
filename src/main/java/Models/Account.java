package Models;

/**
 * Account Model for Fashion Management System
 * @author ADMIN
 */
public class Account {
    
    // 1. Khai báo các thuộc tính (Trường dữ liệu)
    private String accountId; // ĐÃ ĐỔI THÀNH STRING để nhận ID dạng "ACC..."
    private String email;
    private String password;
    private String fullName;
    private String role; // Ví dụ: "Admin", "Customer", "Staff"
    private String status; // ĐÃ ĐỔI THÀNH STRING để nhận chữ "Active"

    // 2. Hàm khởi tạo rỗng (Bắt buộc phải có trong Java Bean)
    public Account() {
    }

    // 3. Hàm khởi tạo đầy đủ tham số
    public Account(String accountId, String email, String password, String fullName, String role, String status) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
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

    // Đã đổi tên hàm từ isStatus() thành getStatus() cho phù hợp với kiểu String
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // 5. Hàm toString (Hỗ trợ in ra console để debug lỗi)
    @Override
    public String toString() {
        return "Account{" + "accountId=" + accountId + ", email=" + email + ", fullName=" + fullName + ", role=" + role + ", status=" + status + '}';
    }
}