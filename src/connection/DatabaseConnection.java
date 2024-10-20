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
    String server = "localhost";
        String port = "3306";
        String database = "chat_application";
        String userName = "truong";
        String password = "123456aA@";
        String url = "jdbc:mysql://" + server + ":" + port + "/" + database 
               + "?allowPublicKeyRetrieval=true&useSSL=false";

        connection = java.sql.DriverManager.getConnection(url, userName, password);
}

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
