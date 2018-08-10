package com.teetov.chat.server.clientbase;

import java.util.HashMap;
import java.util.Map;

public class ClientValidator {
    private Map<String, String> users = new HashMap<>();
    
    
    public ClientValidator(Map<String, String> users) throws ClientbaseException {
        this.users = users;
    }
    
    public boolean isCorrectPassword(String name, String password) {
        if(name == null) 
            return false;
        
        String pass = users.get(name);
        if(pass != null) {
            if(pass.equals(password))
                return true;
        }
        return false;
    }
    
    public boolean isCorrectName(String name) {
        return users.containsKey(name);
    }
}
