package DALs;

import Models.Account;
import Utils.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object cho Account
 * @author ADMIN
 */
public class AccountDAO {
    
    /**
     * Kiểm tra thông tin đăng nhập của người dùng
     * @param email Email do người dùng nhập
     * @param password Mật khẩu do người dùng nhập (đã mã hóa hoặc chưa)
     * @return Đối tượng Account nếu thành công, null nếu thất bại
     */
    public Account checkLogin(String email, String password) {
        
        // 1. Sửa tên bảng thành Accounts và cột mật khẩu thành passwordHash
        String query = "SELECT * FROM Accounts WHERE email = ? AND passwordHash = ?";
        
        try (Connection conn = new DBContext().getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    
                    // 2. Map ĐÚNG tên cột trong SQL Server vào Object
                    
                    /* LƯU Ý QUAN TRỌNG: 
                       Vì accountId lúc nãy đăng ký chúng ta đang để dạng chữ ("ACC" + số), 
                       nên chỗ này phải dùng rs.getString("accountId"). 
                       Nếu class Account (Models) của bạn biến accountId đang là kiểu 'int', 
                       hãy vào đó đổi lại thành kiểu 'String' nhé! 
                    */
                    acc.setAccountId(rs.getString("accountId")); // Thay vì rs.getInt("id")
                    
                    acc.setEmail(rs.getString("email"));
                    
                    // Cột mật khẩu trong DB tên là passwordHash
                    acc.setPassword(rs.getString("passwordHash"));
                    
                    // Cột tên trong DB là fullName (không có dấu gạch dưới)
                    acc.setFullName(rs.getString("fullName"));
                    
                    acc.setRole(rs.getString("role"));
                    
                    /* Tạm thời mình đóng dòng status lại. 
                       Vì trong DB status đang lưu chữ 'Active', 
                       nếu class Account của bạn dùng kiểu boolean sẽ bị lỗi.
                       Bạn có thể mở ra và chỉnh lại sau nếu cần. */
                    // acc.setStatus(rs.getBoolean("status")); 
                    
                    return acc; // Đăng nhập thành công, trả về acc
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
}