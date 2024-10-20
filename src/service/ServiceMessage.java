/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import model.Model_Send_Message;
import connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mrtru
 */
public class ServiceMessage {
    // SQL statements

    private static final String INSERT_TEXT_MESSAGE = "INSERT INTO message (MessageType, FromUserID, ToUserID, Text, Time, FileID) VALUES (?, ?, ?, ?, ?, NULL)";
    private static final String INSERT_FILE_MESSAGE = "INSERT INTO message (MessageType, FromUserID, ToUserID, Text, Time, FileID) VALUES (?, ?, ?, NULL, ?, ?)"; // Text is null for file messages
    private static final String SELECT_MESSAGES_BY_USER = "SELECT * FROM message " +
    "WHERE (FromUserID = ? AND ToUserID = ?) " +
    "OR (FromUserID = ? AND ToUserID = ?) " +
    "ORDER BY Time ASC";
    private final Connection con;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ServiceMessage() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }

    // Method for saving text or emoji messages
    public void saveTextMessage(Model_Send_Message message) throws SQLException {
        try (PreparedStatement p = con.prepareStatement(INSERT_TEXT_MESSAGE)) {
            LocalDateTime localDateTime = LocalDateTime.parse(message.getTime(), formatter);
            Timestamp timestamp = Timestamp.valueOf(localDateTime);
            p.setInt(1, message.getMessageType());
            p.setInt(2, message.getFromUserID());
            p.setInt(3, message.getToUserID());
            p.setString(4, message.getText());
            p.setTimestamp(5, timestamp);

            p.executeUpdate();
        }
    }

    // Method for saving file or image messages
    public void saveFileMessage(Model_Send_Message message) throws SQLException {
        try (PreparedStatement p = con.prepareStatement(INSERT_FILE_MESSAGE)) {
            LocalDateTime localDateTime = LocalDateTime.parse(message.getTime(), formatter);
            Timestamp timestamp = Timestamp.valueOf(localDateTime);
            p.setInt(1, message.getMessageType()); // MessageType for file/image
            p.setInt(2, message.getFromUserID());
            p.setInt(3, message.getToUserID());
            p.setTimestamp(4, timestamp); // Ensure the timestamp is in the correct format
            p.setInt(5, message.getFileID()); // Assuming FileID is part of the message

            p.executeUpdate();
        }
    }

    public List<Model_Send_Message> getMessagesByUser(int userID, int targetUserID) throws SQLException {
        List<Model_Send_Message> messages = new ArrayList<>();

        // SQL query with descending order by time
        try (PreparedStatement p = con.prepareStatement(SELECT_MESSAGES_BY_USER)) {
            // Set parameters for both possible sender-receiver cases
            p.setInt(1, userID);        // Case 1: userID as sender
            p.setInt(2, targetUserID);  // Case 1: targetUserID as receiver
            p.setInt(3, targetUserID);  // Case 2: targetUserID as sender
            p.setInt(4, userID);        // Case 2: userID as receiver

            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    Model_Send_Message message = new Model_Send_Message();

                    // Set message attributes
                    message.setFromUserID(rs.getInt("FromUserID"));
                    message.setToUserID(rs.getInt("ToUserID"));
                    message.setMessageType(rs.getInt("MessageType"));
                    message.setText(rs.getString("Text"));

                    // Handle timestamp conversion and formatting
                    Timestamp timestamp = rs.getTimestamp("Time");
                    if (timestamp != null) {
                        LocalDateTime dateTime = timestamp.toLocalDateTime();
                        message.setTime(dateTime.format(formatter));
                    }

                    // Handle FileID if available
                    int fileID = rs.getInt("FileID");
                    if (!rs.wasNull()) {
                        message.setFileID(fileID);
                    }

                    // Add the message to the list
                    messages.add(message);
                }
            }
        }
        return messages;
    }
}
