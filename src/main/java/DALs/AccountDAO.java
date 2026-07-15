package DALs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import Models.Account;
import Utils.DBContext;

/**
 * Data Access Object cho Account
 * @author ADMIN
 */
public class AccountDAO {

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account acc = new Account();
        acc.setAccountId(rs.getString("accountId"));
        acc.setEmail(rs.getString("email"));
        acc.setPassword(rs.getString("passwordHash"));
        acc.setFullName(rs.getString("fullName"));
        acc.setRole(rs.getString("role"));
        acc.setStatus(rs.getString("status"));
        acc.setPhone(rs.getString("phone"));
        return acc;
    }

    public Account checkLogin(String email, String password) {
        String query = "SELECT accountId, email, passwordHash, fullName, role, status, phone FROM Accounts WHERE email = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("passwordHash");
                    if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
                        return mapAccount(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.checkLogin: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Lỗi tại AccountDAO.checkLogin: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Account getAccountById(String accountId) {
        String query = "SELECT accountId, email, passwordHash, fullName, role, status, phone FROM Accounts WHERE accountId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAccount(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.getAccountById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProfile(Account account) {
        String query = "UPDATE Accounts SET fullName = ?, phone = ? WHERE accountId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, account.getFullName());
            ps.setString(2, account.getPhone());
            ps.setString(3, account.getAccountId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.updateProfile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String accountId, String newPassword) {
        String query = "UPDATE Accounts SET passwordHash = ? WHERE accountId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, newPassword);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.updatePassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String query = "SELECT accountId, email, passwordHash, fullName, role, status, phone FROM Accounts ORDER BY accountId";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.getAllAccounts: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateRole(String accountId, String role) {
        String query = "UPDATE Accounts SET role = ? WHERE accountId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, role);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.updateRole: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(String accountId, String status) {
        String query = "UPDATE Accounts SET status = ? WHERE accountId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.updateStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}