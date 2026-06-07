package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    // Khai báo biến connection
    protected Connection connection;
    
    public DBContext() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=FashionShopDB;"
                    + "user=sa;"
                    + "password=123456;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Lỗi kết nối DB: " + ex.getMessage());
        }
    }    

    // SỬA LẠI HÀM NÀY: Xóa bỏ chữ "static" và trả về biến connection ở trên
    public Connection getConnection() {
        return connection; 
    }
}