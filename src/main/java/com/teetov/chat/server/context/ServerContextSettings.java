package com.teetov.chat.server.context;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;

/**
 * Класс предназначен для передачи основных параметров сервера новому экземпляру {@code ServerContext}.
 * @author  Aleksey Titov
 *
 */
public class ServerContextSettings {
    
    private String[] args;
    
    private final String PASSWORD_ARG = "-password";
    private final String LOGIN_ARG = "-login";
    
    private final int DEFAULT_PORT = 34543;
    
    private final int DEFAULT_MAX_CONNECTION = 20;
    
    private    ServerProprertyExtructer properties = new ServerProprertyExtructer();        
            
    private String password;
    
    private AccessType access;
    
    private boolean hasAccessOption = false;
    
    private int port;
    
    private int maxConnection;
    
    public ServerContextSettings(String[] args) {
        
        this.args = args;
        
        prepereServerSetting();
    }

    private void prepereServerSetting() {
        processArgs();
        
        if(!hasAccessOption) 
            retrieveAccessProperty();
        
        if(!hasAccessOption) 
            setAccessType(AccessType.SIMPLE);
        
        retrievePortProperty();
        
        retrieveMaxConnection();
    }
    
    public AccessType getAccessOptions() {
        return access;
    }

    public String getPassord() {
        return password;
    }

    
    public int getPort() {
        return port;
    }
    
    /**
     * Анализирует аргументы командной стороки.
     * @param args список аргументов
     */
    private void processArgs() {
        
        for(String arg : args) {
            if(!hasAccessOption)
                processArgumentAccess(arg);
        }
    
    }
    
    /**
     * Извлекает информацию о серверном порте из server.properties.
     * Проверяет на корректность. В случае ошибки назначает серверным портом порт по-умолчанию.
     */
    private void retrievePortProperty() {
        try {
            String portStr = properties.getPort();
            
            int tempPort = Integer.valueOf(portStr);
            
            if(tempPort < 0 || tempPort > 65535) 
                port = DEFAULT_PORT;
            else 
                port = tempPort;
        } catch (Exception e) {
            LogManager.getLogger().error("Cant extract port from properties file", e);
            port = DEFAULT_PORT;
        }
    }
    
    /**
     * Извлекает информацию о режиме доступа из server.properties.
     * <p>В случае успешного получения необходимых параметров устанавливает {@code hasAccessOption} = {@code true}.</p>
     */
    private void retrieveAccessProperty() {
        try {
            String assess = properties.getAccessType();
            
            if(properties.ACCESS_LOGIN.equals(assess)) {

                setAccessType(AccessType.LOGIN);
                return;
            }
            
            if(properties.ACCESS_PASSORD.equals(assess)) {
                String passwordProp = properties.getPassword();

                setAccessType(AccessType.PASSWORD);
                
                password = passwordProp;
                return;
            }
            
        } catch (IOException e) {
            LogManager.getLogger().error("Cant find properties file", e);
        }

    }
    
    /**
     * Проверяет, относится ли аргумент командной строки к управлению режимом доступа.
     * В случае соответствия, заполняет необходимые поля (authent, password).
     * Если аргумент {@code -password} не содержит пароля, пароль будет получен из server.properties файла.
     * Если информаии о пароле там не окажется {@code -password} будет проигнорирован.
     * 
     * <p>В случае успешного получения необходимых параметров устанавливает {@code hasAccessOption} = {@code true}.</p>
     * @param arg аргумент командной строки
     */
    private void processArgumentAccess(String arg) {
        
        if(arg.startsWith(PASSWORD_ARG)) {
            
            if(arg.matches(PASSWORD_ARG)) {
                try {
                    
                    String passwordProp = properties.getPassword();
                    if(passwordProp != null && !passwordProp.equals("")) {
                        
                        setAccessType(AccessType.PASSWORD);
                        password = passwordProp;

                        return;
                    }
                    
                } catch (IOException e) {
                    LogManager.getLogger().error("Cant find properties file", e);
                }
                
                
            } else {
                System.out.println(PASSWORD_ARG + "=.+" + " : " + arg);
                if(arg.matches(PASSWORD_ARG + "=.+")) {

                    setAccessType(AccessType.PASSWORD);
                    password = arg.substring((PASSWORD_ARG + "=").length());

                    return;
                }
            }
            
            System.out.println("Для запуска приложения с ключом -password необходимо,"
                    + "\r\nлибо указать пароль после '=' (-passord=[your password]),"
                    + "\r\nлибо указать его в server.properties файле.");
            return;
            
        }
        
        if(arg.matches(LOGIN_ARG)) {
            setAccessType(AccessType.LOGIN);
            
            return;
        }
    }

    
    private void setAccessType(AccessType type) {
        access = type;

        hasAccessOption = true;
    }
    
    private void retrieveMaxConnection() {
        try {
            String maxConn = properties.getMaxConnections();
            
            maxConnection = Integer.valueOf(maxConn);
            
        } catch (Exception e) {
            LogManager.getLogger().error("Can not retrieve maxConnection property", e);
            maxConnection = DEFAULT_MAX_CONNECTION;
        }
    }
    
    public int getMaxConnection() {
        return maxConnection;
    }
    
    @Override
    public String toString() {
        return "ServerContextProperties [password=" + password + ", accessType=" + access + ", port=" + port + ", maxConnection=" + maxConnection + "]";
    }

}

