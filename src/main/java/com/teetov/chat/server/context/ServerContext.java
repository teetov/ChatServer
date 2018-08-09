package com.teetov.chat.server.context;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

import com.teetov.chat.message.Message;
import com.teetov.chat.message.MessageFactory;
import com.teetov.chat.server.clientbase.ClientsCheckerProducer;
import com.teetov.chat.server.connections.AccessChecker;
import com.teetov.chat.server.connections.ClientListener;
import com.teetov.chat.server.connections.ConnectionThread;
import com.teetov.chat.server.connections.ConnectionsManager;

/**
 * Основной класс-хранилище для работы с сервером. 
 * Содержит как характеристики подключения (режим доступа, возможный пароль), 
 * так и экземпляры вспомогательных класов ({@code ConnectionsManager}, 
 * {@code ClientListener}, {@code MessageFactory}).
 * @author  Aleksey Titov
 *
 */
public class ServerContext implements Closeable {
    private ServerSocket server;
    
    private ConnectionsManager connections;    
    
    private MessageFactory serverMessage;

    private ClientListener listener;
    
    private ClientsCheckerProducer clientsProducer = new ClientsCheckerProducer();
    
    private AccessType access;
    
    private String password;
    
    public ServerContext(ServerContextSettings prop) throws IOException {
        server = new ServerSocket(prop.getPort());
        listener = new ClientListener(this);    
        
        connections = new ConnectionsManager(prop.getMaxConnection());
        
        serverMessage = new MessageFactory("server", server.getInetAddress().toString());
        
        access = prop.getAccessOptions();
        
        password = prop.getPassord();
    }    
    
    public ServerSocket getServerSocket() {
        return server;
    }

    public ConnectionsManager getConnectionsManager() {
        return connections;
    }
    
    public void addConnection(ConnectionThread connection) {
        connections.add(connection);
    }

    public AccessChecker getAccessChecker() {
        return new AccessChecker(this, clientsProducer);
    }
    
    public Message getMessage(String text) {
        return serverMessage.getMesage(text);
    }
    
    public Message getMessage(String text, int destination) {
        return serverMessage.getMesage(text, destination);
    }
    
    public ClientListener getListener() {
        return listener;
    }
    
    public AccessType getAccessOptions() {
        return access;
    }
    
    public String getPassword() {
        return password;
    }
    
    /**
     * Определяет необходим ли пароль для поключения к серверу.
     * @return {@code true}, если от пользователя требуется пароль
     */
    public boolean withPassword() {
        if(access == AccessType.LOGIN || access == AccessType.PASSWORD)
            return true;
        return false;
    }
    
    @Override
    public void close(){
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }
    
}
