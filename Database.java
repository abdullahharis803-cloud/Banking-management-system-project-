import java.sql.*;

public class Database {

    static final String URL  = "jdbc:mysql://localhost:3306/bankdb";
    static final String USER = "root";
    static final String PASS = "password";  // change this to your password

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
