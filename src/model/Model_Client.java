/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import com.corundumstudio.socketio.SocketIOClient;

/**
 *
 * @author mrtru
 */
public class Model_Client {
    private SocketIOClient client;
    private Model_User_Account user;
    
    public SocketIOClient getClient() {
        return client;
    }

    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    public Model_User_Account getUser() {
        return user;
    }

    public void setUser(Model_User_Account user) {
        this.user = user;
    }

    public Model_Client(SocketIOClient client, Model_User_Account user) {
        this.client = client;
        this.user = user;
    }

    public Model_Client() {
    }

    
}
