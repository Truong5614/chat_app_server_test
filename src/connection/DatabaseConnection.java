/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package connection;
import java.sql.Connection;
import java.sql.SQLException;
/**
 *
 * @author mrtru
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private DatabaseConnection() {

    }

    public void connectToDatabase() throws SQLException {
   String server = "autorack.proxy.rlwy.net";
        String port = "41508";
        String database = "railway";
        String userName = "root";
        String password = "TMHsGQfWToXxpRIXPdMFaBoECOusTRMM";
        String url = "jdbc:mysql://" + server + ":" + port + "/" + database 
               + "?allowPublicKeyRetrieval=true&useSSL=true&requireSSL=true";

        connection = java.sql.DriverManager.getConnection(url, userName, password);
}

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
