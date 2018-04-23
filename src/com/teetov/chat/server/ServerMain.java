package com.teetov.chat.server;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import com.teetov.chat.server.connections.ClientListener;
import com.teetov.chat.server.context.ServerContext;
import com.teetov.chat.server.context.ServerContextSettings;

public class ServerMain {
	
	public static void main(String[] args) {
		
		ServerContextSettings properties = new ServerContextSettings(args);
		
		LogManager.getLogger().info("Server options has been initialized : {}", properties);
		
		try(ServerContext context = new ServerContext(properties)) {
			
			
			ClientListener listener = context.getListener();
			
			while (true) {
				listener.listen();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
