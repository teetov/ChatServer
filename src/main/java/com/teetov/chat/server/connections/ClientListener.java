package com.teetov.chat.server.connections;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;

import com.teetov.chat.server.context.ServerContext;

/**
 * Класс предназначен идя создания и запуска новых потоков {@code ConnectionThread}.
 * ClientListener слушает серверный порт. После получения от него нового сединения, 
 * запускает поток, в котором происходит общение с клиентом.
 *
 */
public class ClientListener {    
    
    
    private ServerContext context;
    
    public ClientListener(ServerContext context) {
        this.context = context;
    }
            
    /**
     * Метод предназначен для запуска потока с новоподсоеденившимся клиентом.
     * После вызова {@code listen()} начинает слушать серверный порт. Дождавшись подключения следующего клиента,
     * он создаёт объект {@code ConnectionThread} и запускает его в новом потоке.
     */
    public void listen() {
        try {
            ConnectionThread connection = connection();
            new Thread(connection).start();
        } catch (IOException e) {
            LogManager.getLogger().error("Connection failed", e);
        }
    }
    
    /**
     * Создаёт новый {@code ConnectionThread}, слушая  {@code ServerSocket}.
     * @return {@code ConnectionThread} с новым клиентом
     * @throws IOException
     */
    private ConnectionThread connection() throws IOException {
        Socket socket = context.getServerSocket().accept();
            
        return new ConnectionThread(context, socket);
    }
    
    
}
