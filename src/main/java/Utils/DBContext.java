package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    protected Connection connection;

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=FashionShopDB;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123456";

    public DBContext() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQL Server JDBC Driver was not found. Please add sqljdbc driver to the project library.", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to SQL Server. Please check databaseName, username, password, SQL Server service, and port 1433. Current DB_URL = " + DB_URL, e);
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database connection is null. DBContext could not create a valid SQL Server connection.");
        }
        return connection;
    }
}