package ma.covid19.api;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection connection = null;

    public static void connect() {
        try {
            String url = "jdbc:sqlite:" + new File("").getAbsolutePath() + "/database.db";
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        while (connection == null) ;
    }
}
