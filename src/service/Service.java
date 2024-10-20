/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import app.MessageType;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import model.Model_Client;
import model.Model_File;
import model.Model_Login;
import model.Model_Message;
import model.Model_Package_Sender;
import model.Model_Receive_File;
import model.Model_Receive_Image;
import model.Model_Receive_Message;
import model.Model_Register;
import model.Model_Request_File;
import model.Model_Send_Message;
import model.Model_User_Account;

/**
 *
 * @author mrtru
 */
public class Service {

    private static Service instance;
    private SocketIOServer server;
    private ServiceUser serviceUser;
    private ServiceFIle serviceFile;
    private ServiceMessage serviceMessage;
    private List<Model_Client> listClient;
    private JTextArea textArea;
    private final int PORT_NUMBER = 9999;

    public static Service getInstance(JTextArea textArea) {
        if (instance == null) {
            instance = new Service(textArea);
        }
        return instance;
    }

    private Service(JTextArea textArea) {
        this.textArea = textArea;
        serviceUser = new ServiceUser();
        serviceFile = new ServiceFIle();
        serviceMessage = new ServiceMessage();
        listClient = new ArrayList<>();
    }

    public void startServer() {
        Configuration config = new Configuration();
        config.setPort(PORT_NUMBER);
        server = new SocketIOServer(config);
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient sioc) {
                textArea.append("One client connected\n");
            }
        });
        server.addEventListener("register", Model_Register.class, new DataListener<Model_Register>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Register t, AckRequest ar) throws Exception {
                Model_Message message = serviceUser.register(t);
                ar.sendAckData(message.isAction(), message.getMessage(), message.getData());
                if (message.isAction()) {
                    textArea.append("User has Register :" + t.getUserName() + " Pass :" + t.getPassword() + "\n");
                    server.getBroadcastOperations().sendEvent("list_user", (Model_User_Account) message.getData());
                    addClient(sioc, (Model_User_Account) message.getData());
                }
            }
        });
        server.addEventListener("login", Model_Login.class, new DataListener<Model_Login>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Login t, AckRequest ar) throws Exception {
                Model_User_Account login = serviceUser.login(t);
                if (login != null) {
                    ar.sendAckData(true, login);
                    addClient(sioc, login);
                    userConnect(login.getUserID());
                } else {
                    ar.sendAckData(false);
                }
            }
        });
        server.addEventListener("list_user", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient sioc, Integer userID, AckRequest ar) throws Exception {
                try {
                    List<Model_User_Account> list = serviceUser.getUser(userID);
                    sioc.sendEvent("list_user", list.toArray());
                } catch (SQLException e) {
                    System.err.println(e);
                }
            }
        });
        server.addEventListener("send_to_user", Model_Send_Message.class, new DataListener<Model_Send_Message>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Send_Message t, AckRequest ar) throws Exception {
                sendToClient(t, ar);
            }
        });
        server.addEventListener("send_file", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Package_Sender t, AckRequest ar) throws Exception {
                try {
                    serviceFile.receiveFile(t);
                    if (t.isFinish()) {
                        ar.sendAckData(true);
                        Model_Receive_Image dataImage = new Model_Receive_Image();
                        Model_Receive_File dataFile = new Model_Receive_File();
                        dataFile.setFileID(t.getFileID());
                        System.out.println(t.getFileID());
                        dataImage.setFileID(t.getFileID());
                        dataImage.setFileName(serviceFile.getFileName(t.getFileID()) + serviceFile.getFile(t.getFileID()).getFileExtension());
                        Model_Send_Message message = serviceFile.closeFile(dataImage);
                        //  Send to client 'message'
                        if (message.getMessageType() == MessageType.IMAGE.getValue()) {
                            System.out.println(t.getFileID());
                            message.setFileID(t.getFileID());
                            serviceMessage.saveFileMessage(message);
                            sendTempFileToClient(message, dataImage);
                        } else if (message.getMessageType() == MessageType.FILE.getValue()) {
                            System.out.println(t.getFileID());
                            message.setFileID(t.getFileID());
                            serviceMessage.saveFileMessage(message);
                            sendTempFileToClient(message, dataFile);
                        }

                    } else {
                        ar.sendAckData(true);
                    }
                } catch (IOException | SQLException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }
            }
        });
        server.addEventListener("get_file", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient sioc, Integer t, AckRequest ar) throws Exception {
                Model_File file = serviceFile.initFile(t);
                long fileSize = serviceFile.getFileSize(t);
                String filename = serviceFile.getFileName(t);
                ar.sendAckData(file.getFileExtension(), fileSize, filename);
            }
        });
        server.addEventListener("request_file", Model_Request_File.class, new DataListener<Model_Request_File>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Request_File t, AckRequest ar) throws Exception {
                byte[] data = serviceFile.getFileData(t.getCurrentLength(), t.getFileID());
                String filename = serviceFile.getFileName(t.getFileID());
                if (data != null) {
                    ar.sendAckData(data);
                } else {
                    ar.sendAckData();
                }
            }
        });
        server.addEventListener("user_click", int[].class, new DataListener<int[]>() {
            @Override
            public void onData(SocketIOClient client, int[] userIDs, AckRequest ackRequest) throws Exception {
                if (userIDs.length < 2) {
                    System.out.println("Not enough user IDs provided.");
                    ackRequest.sendAckData(false);
                    return;
                }
                int fromUserID = userIDs[0];
                int toUserID = userIDs[1];
                System.out.println("User clicked: FromUserID = " + fromUserID + ", ToUserID = " + toUserID);
                ServiceMessage serviceMessage = new ServiceMessage();
                List<Model_Send_Message> messages = serviceMessage.getMessagesByUser(fromUserID,toUserID);
                for (Model_Send_Message message : messages) {
                    if (message.getFileID() > 0) { // Assuming fileID is a positive integer
                        String fileName = serviceFile.getFileName(message.getFileID());
                        String fileExtension = serviceFile.getFile(message.getFileID()).getFileExtension();

                        if (fileName != null && fileExtension != null) {
                            message.setFileName(fileName + fileExtension);
                        } else {
                            System.err.println("File name or extension is null for FileID: " + message.getFileID());
                        }
                    }
                }
                client.sendEvent("receive_messages", messages);
                ackRequest.sendAckData(true);
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient sioc) {
                int userID = removeClient(sioc);
                if (userID != 0) {
                    userDisconnect(userID);
                }
            }
        });
        server.start();
        textArea.append("Server has Start on port : " + PORT_NUMBER + "\n");
    }

    private void userConnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, true);
    }

    private void userDisconnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }

    private void addClient(SocketIOClient client, Model_User_Account user) {
        listClient.add(new Model_Client(client, user));
    }

    private void sendToClient(Model_Send_Message data, AckRequest ar) throws SQLException {
        if (data.getMessageType() == MessageType.IMAGE.getValue() || data.getMessageType() == MessageType.FILE.getValue()) {
            try {
                Model_File file = serviceFile.addFileReceiver(data.getText());
                serviceFile.initFile(file, data);
                ar.sendAckData(file.getFileID());
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            for (Model_Client c : listClient) {
                if (c.getUser().getUserID() == data.getToUserID()) {
                    serviceMessage.saveTextMessage(data);
                    c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), null, null, data.getTime()));
                    break;
                }
            }
        }
    }

    private void sendTempFileToClient(Model_Send_Message data, Model_Receive_Image dataImage) throws SQLException {
        for (Model_Client c : listClient) {
            if (c.getUser().getUserID() == data.getToUserID()) {
                c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), dataImage, null, data.getTime()));
                break;
            }
        }
    }

    private void sendTempFileToClient(Model_Send_Message data, Model_Receive_File dataFile) throws SQLException {
        for (Model_Client c : listClient) {
            if (c.getUser().getUserID() == data.getToUserID()) {
                c.getClient().sendEvent("receive_ms",
                        new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), null, dataFile, data.getTime()));
                break;
            }
        }
    }

    public int removeClient(SocketIOClient client) {
        for (Model_Client d : listClient) {
            if (d.getClient() == client) {
                listClient.remove(d);
                return d.getUser().getUserID();
            }
        }
        return 0;
    }

    public List<Model_Client> getListClient() {
        return listClient;
    }
}
