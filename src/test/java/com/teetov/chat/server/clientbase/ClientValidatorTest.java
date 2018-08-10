package com.teetov.chat.server.clientbase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

public class ClientValidatorTest {

    private ClientValidator checker;
    
    private Map<String, String> users;
    
    @BeforeEach
    public void prepereChecker() {
        users = new HashMap<>();
        users.put("admin", "qwerty");
        
        try {
            checker = new ClientValidator(users);
        } catch (ClientbaseException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void isCorrectNameTest() {
        boolean access = checker.isCorrectName("admin");
        assertTrue(access);

        access = checker.isCorrectName("user");
        assertFalse(access);
    }
    
    @Test
    public void isCorrectPasswordTest() {
        boolean access = checker.isCorrectPassword("admin", "qwerty");
        assertTrue(access);

        access = checker.isCorrectPassword("admin", "qwertyy");
        assertFalse(access);
        

        access = checker.isCorrectPassword("user", "qwerty");
        assertFalse(access);
    }
}
