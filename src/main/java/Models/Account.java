package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Model for Fashion Management System
 * @author ADMIN
 */
public class Account {

    private String accountId;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;
    private String status;
    private String phone;
    private String address;
    private String avatar;
    private BigDecimal salary;
    private LocalDateTime createdAt;

    public Account() {
    }

    public Account(String accountId, String username, String email, String password, String fullName,
                   String role, String status, String phone, String address, String avatar,
                   BigDecimal salary, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.phone = phone;
        this.address = address;
        this.avatar = avatar;
        this.salary = salary;
        this.createdAt = createdAt;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Account{" + "accountId=" + accountId + ", email=" + email + ", fullName=" + fullName + ", role=" + role + ", status=" + status + '}';
    }
}
