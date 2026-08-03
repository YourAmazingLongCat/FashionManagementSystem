package DALs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        acc.setUsername(rs.getString("username"));
        acc.setEmail(rs.getString("email"));
        acc.setPassword(rs.getString("passwordHash"));
        acc.setFullName(rs.getString("fullName"));
        acc.setRole(rs.getString("role"));
        acc.setStatus(rs.getString("status"));
        acc.setPhone(rs.getString("phone"));
        acc.setAddress(rs.getString("address"));
        acc.setAvatar(rs.getString("avatar"));
        
        double salary = rs.getDouble("salary");
        acc.setSalary(rs.wasNull() ? null : java.math.BigDecimal.valueOf(salary));
        
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        acc.setCreatedAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null);
        
        return acc;
    }

    public Account checkLogin(String email, String password) {
        String query = "SELECT accountId, username, email, passwordHash, fullName, role, status, phone, "
                     + "address, avatar, salary, createdAt FROM Accounts WHERE email = ?";

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
        String query = "SELECT accountId, username, email, passwordHash, fullName, role, status, phone, "
                     + "address, avatar, salary, createdAt FROM Accounts WHERE accountId = ?";

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

    public Account getAccountByEmail(String email) {
        String query = "SELECT accountId, username, email, passwordHash, fullName, role, status, phone, "
                     + "address, avatar, salary, createdAt FROM Accounts WHERE email = ?";

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAccount(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.getAccountByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProfile(Account account) {
        String query = "UPDATE Accounts SET fullName = ?, phone = ?, address = ? WHERE accountId = ?";

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, account.getFullName());
            ps.setString(2, account.getPhone());
            ps.setString(3, account.getAddress());
            ps.setString(4, account.getAccountId());
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
        String query = "SELECT accountId, username, email, passwordHash, fullName, role, status, phone, "
                     + "address, avatar, salary, createdAt FROM Accounts ORDER BY accountId";

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

    private static final java.util.Set<String> VALID_STATUSES = java.util.Set.of("Active", "Inactive");

    public boolean updateStatus(String accountId, String status) {
        // Normalize: map "Banned" or other UI labels to a valid DB status
        String dbStatus = mapToValidStatus(status);
        if (dbStatus == null) {
            System.out.println("AccountDAO.updateStatus: invalid status '" + status + "'");
            return false;
        }

        String query = "UPDATE Accounts SET status = ? WHERE accountId = ?";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, dbStatus);
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.updateStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String mapToValidStatus(String status) {
        if (status == null) return null;
        String normalized = status.trim();
        if (VALID_STATUSES.contains(normalized)) return normalized;
        if (normalized.equalsIgnoreCase("Banned")
                || normalized.equalsIgnoreCase("Locked")
                || normalized.equalsIgnoreCase("Suspended")
                || normalized.equalsIgnoreCase("Disabled")) {
            return "Inactive";
        }
        return null;
    }

    public boolean isValidStatus(String status) {
        return mapToValidStatus(status) != null;
    }

    public boolean createAccount(Account account, String rawPassword) {
        String query = "INSERT INTO Accounts (accountId, username, email, passwordHash, fullName, role, status, "
                     + "phone, address, avatar, salary, createdAt) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getUsername() != null ? account.getUsername() : account.getEmail());
            ps.setString(3, account.getEmail());
            ps.setString(4, hashed);
            ps.setString(5, account.getFullName());
            ps.setString(6, account.getRole() != null ? account.getRole() : "Customer");
            ps.setString(7, account.getStatus() != null ? account.getStatus() : "Active");
            ps.setString(8, account.getPhone() != null ? account.getPhone() : null);
            ps.setString(9, account.getAddress() != null ? account.getAddress() : null);
            ps.setString(10, account.getAvatar() != null ? account.getAvatar() : null);
            
            if (account.getSalary() != null) {
                ps.setBigDecimal(11, account.getSalary());
            } else {
                ps.setBigDecimal(11, java.math.BigDecimal.ZERO);
            }
            
            ps.setTimestamp(12, account.getCreatedAt() != null 
                    ? Timestamp.valueOf(account.getCreatedAt()) 
                    : Timestamp.valueOf(LocalDateTime.now()));
            
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

    public boolean phoneExists(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String query = "SELECT 1 FROM Accounts WHERE phone = ?";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.phoneExists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean phoneExistsForOtherAccount(String phone, String excludeAccountId) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String query = "SELECT 1 FROM Accounts WHERE phone = ? AND accountId != ?";
        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phone);
            ps.setString(2, excludeAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại AccountDAO.phoneExistsForOtherAccount: " + e.getMessage());
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

    public List<Account> searchAccounts(String keyword, String status) {
        List<Account> list = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT accountId, username, email, passwordHash, fullName, role, status, phone, "
              + "address, avatar, salary, createdAt FROM Accounts WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (email LIKE ? OR phone LIKE ? OR fullName LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status.trim())) {
            query.append(" AND status = ?");
            params.add(status.trim());
        }
        query.append(" ORDER BY accountId");

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public int getTotalAccounts(String keyword, String status) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM Accounts WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (email LIKE ? OR phone LIKE ? OR fullName LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status.trim())) {
            query.append(" AND status = ?");
            params.add(status.trim());
        }

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
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

    public List<Account> getAccountsPaged(int page, int pageSize, String keyword, String status) {
        List<Account> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        StringBuilder query = new StringBuilder(
                "SELECT accountId, username, email, passwordHash, fullName, role, status, phone, "
              + "address, avatar, salary, createdAt FROM Accounts WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (email LIKE ? OR phone LIKE ? OR fullName LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status.trim())) {
            query.append(" AND status = ?");
            params.add(status.trim());
        }
        query.append(" ORDER BY accountId OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
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
