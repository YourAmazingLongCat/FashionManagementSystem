package Models;

/**
 * Account Model for Fashion Management System
 * @author ADMIN
 */
public class Account {

    private String accountId;
    private String email;
    private String password;
    private String fullName;
    private String role;
    private String status;
    private String phone;
    private String address;
    private String avatar;

    public Account() {
    }

    public Account(String accountId, String email, String password, String fullName, String role, String status) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "Account{" + "accountId=" + accountId + ", email=" + email + ", fullName=" + fullName + ", role=" + role + ", status=" + status + '}';
    }
}