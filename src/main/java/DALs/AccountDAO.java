package DAOs;

import Models.Account;
import Utils.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Account
 * @author ADMIN
 */
public class AccountDAO {
    
    /**
     * Kiểm tra thông tin đăng nhập của người dùng
     */
    public Account checkLogin(String email, String password) {
        String query = "SELECT * FROM Accounts WHERE email = ? AND passwordHash = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getString("accountId"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPassword(rs.getString("passwordHash")); // Sửa lại nếu class của bạn là setPasswordHash
                    acc.setFullName(rs.getString("fullName"));
                    acc.setRole(rs.getString("role"));
                    acc.setStatus(rs.getString("status")); 
                    acc.setAvatar(rs.getString("avatar"));
                    acc.setPhone(rs.getString("phone"));
                    return acc;
                }
            }
        } catch (Exception e) { 
            System.out.println("Lỗi tại AccountDAO.checkLogin: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy thông tin tài khoản chỉ bằng Email
     */
    public Account getAccountByEmail(String email) {
        String query = "SELECT * FROM Accounts WHERE email = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getString("accountId"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPassword(rs.getString("passwordHash"));
                    acc.setFullName(rs.getString("fullName"));
                    acc.setRole(rs.getString("role"));
                    acc.setStatus(rs.getString("status"));
                    acc.setAvatar(rs.getString("avatar"));
                    acc.setPhone(rs.getString("phone"));
                    return acc;
                }
            }
        } catch (Exception e) { 
            System.out.println("Lỗi tại AccountDAO.getAccountByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy thông tin tài khoản bằng AccountId (Được thêm mới phục vụ cho Đổi mật khẩu)
     * @param accountId ID của tài khoản
     * @return Account nếu tìm thấy, ngược lại trả về null
     */
    public Account getAccountById(String accountId) {
        String query = "SELECT * FROM Accounts WHERE accountId = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, accountId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getString("accountId"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPassword(rs.getString("passwordHash"));
                    acc.setFullName(rs.getString("fullName"));
                    acc.setRole(rs.getString("role"));
                    acc.setStatus(rs.getString("status"));
                    acc.setAvatar(rs.getString("avatar"));
                    acc.setPhone(rs.getString("phone"));
                    return acc;
                }
            }
        } catch (Exception e) { 
            System.out.println("Lỗi tại AccountDAO.getAccountById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật thông tin Hồ sơ cá nhân (Tên, Số điện thoại, Ảnh đại diện)
     */
    public boolean updateProfile(Account acc) {
        String query = "UPDATE Accounts SET fullName = ?, phone = ?, avatar = ?, updatedAt = GETDATE() WHERE accountId = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, acc.getFullName());
            ps.setString(2, acc.getPhone());
            ps.setString(3, acc.getAvatar());
            ps.setString(4, acc.getAccountId());
            
            int rowAffected = ps.executeUpdate();
            return rowAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi tại AccountDAO.updateProfile: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đổi mật khẩu tài khoản (Được thêm mới phục vụ cho Đổi mật khẩu)
     * @param accountId ID của tài khoản
     * @param newPassword Mật khẩu mới cần lưu
     * @return true nếu update thành công, false nếu thất bại
     */
    public boolean updatePassword(String accountId, String newPassword) {
        String query = "UPDATE Accounts SET passwordHash = ?, updatedAt = GETDATE() WHERE accountId = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, accountId);
            
            int rowAffected = ps.executeUpdate();
            return rowAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi tại AccountDAO.updatePassword: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /* ================= CÁC HÀM THÊM MỚI CHO TRANG ADMIN ================= */

    /**
     * Lấy danh sách tất cả tài khoản
     */
    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String query = "SELECT * FROM Accounts";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Account acc = new Account();
                acc.setAccountId(rs.getString("accountId"));
                acc.setEmail(rs.getString("email"));
                acc.setFullName(rs.getString("fullName"));
                acc.setRole(rs.getString("role"));
                acc.setStatus(rs.getString("status"));
                acc.setPhone(rs.getString("phone"));
                list.add(acc);
            }
        } catch (Exception e) {
            System.out.println("Lỗi tại getAllAccounts: " + e.getMessage());
        }
        return list;
    }

    /**
     * Cập nhật Quyền (Role) cho tài khoản
     */
    public boolean updateRole(String accountId, String role) {
        String query = "UPDATE Accounts SET role = ? WHERE accountId = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, role);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi tại updateRole: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cập nhật Trạng thái (Status) - Dùng để khóa/mở khóa
     */
    public boolean updateStatus(String accountId, String status) {
        String query = "UPDATE Accounts SET status = ? WHERE accountId = ?";
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi tại updateStatus: " + e.getMessage());
        }
        return false;
    }
}