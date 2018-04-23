package com.teetov.chat.server.context;

import java.io.IOException;
import java.util.Properties;

public class ServerProprertyExtructer {

	private String propPath = "/server.properties";
	
	private Properties prop = new Properties();
	
	public final String ACCESS = "accessType";
	public final String ACCESS_PASSORD = "password";
	public final String ACCESS_LOGIN = "login";
	
	public final String MAX_ACTIVE_CONNECTIONS = "maxConnections";

	public final String PORT = "port";
	
	public final String PASSWORD_PROP_VALUE = "passValue";

	private void load() throws IOException {
		prop.load(ServerProprertyExtructer.class.getResourceAsStream(propPath));
	}
	
	public String getPort() throws IOException {
		load();
		String port = prop.getProperty(PORT);
		return port;
	}
	
	public String getAccessType() throws IOException {
		load();
		return prop.getProperty(ACCESS);
	}
	
	public String getPassword() throws IOException {
		load();
		return prop.getProperty(PASSWORD_PROP_VALUE);
	}
	
	public String getMaxConnections() throws IOException {
		load();
		return prop.getProperty(MAX_ACTIVE_CONNECTIONS);
	}
}
