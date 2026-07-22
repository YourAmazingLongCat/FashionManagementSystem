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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

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

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.updateStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAccount(Account account, String rawPassword) {
        String query = "INSERT INTO Accounts (accountId, username, email, passwordHash, fullName, role, status, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getEmail());
            ps.setString(4, hashed);
            ps.setString(5, account.getFullName());
            ps.setString(6, account.getRole());
            ps.setString(7, account.getStatus() != null ? account.getStatus() : "Active");
            ps.setString(8, account.getPhone() != null ? account.getPhone() : "");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.createAccount: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String query = "SELECT 1 FROM Accounts WHERE email = ?";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.emailExists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String generateNextAccountId() {
        String query = "SELECT TOP 1 accountId FROM Accounts ORDER BY accountId DESC";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("accountId");
                String numericPart = lastId.replaceAll("[^0-9]", "");
                if (numericPart.isEmpty()) {
                    return "ACC00001";
                }
                long nextNum = Long.parseLong(numericPart) + 1;
                return "ACC" + String.format("%05d", nextNum);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.generateNextAccountId: " + e.getMessage());
            e.printStackTrace();
        }
        return "ACC00001";
    }

    public List<Account> searchAccounts(String keyword) {
        List<Account> list = new ArrayList<>();
        String query = "SELECT accountId, email, passwordHash, fullName, role, status, phone FROM Accounts WHERE email LIKE ? OR phone LIKE ? ORDER BY accountId";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            String kw = "%" + (keyword != null ? keyword : "") + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAccount(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.searchAccounts: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteAccount(String accountId) {
        String query = "DELETE FROM Accounts WHERE accountId = ?";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.deleteAccount: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalAccounts(String keyword) {
        String query = keyword != null && !keyword.trim().isEmpty()
                ? "SELECT COUNT(*) FROM Accounts WHERE email LIKE ? OR phone LIKE ?"
                : "SELECT COUNT(*) FROM Accounts";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.getTotalAccounts: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public List<Account> getAccountsPaged(int page, int pageSize, String keyword) {
        List<Account> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String baseQuery = "SELECT accountId, email, passwordHash, fullName, role, status, phone FROM Accounts";
        String query;
        if (keyword != null && !keyword.trim().isEmpty()) {
            query = baseQuery + " WHERE email LIKE ? OR phone LIKE ? ORDER BY accountId OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        } else {
            query = baseQuery + " ORDER BY accountId OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        }
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
                ps.setInt(3, offset);
                ps.setInt(4, pageSize);
            } else {
                ps.setInt(1, offset);
                ps.setInt(2, pageSize);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAccount(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.getAccountsPaged: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}