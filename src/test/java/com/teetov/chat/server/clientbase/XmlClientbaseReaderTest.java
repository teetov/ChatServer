package com.teetov.chat.server.clientbase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class XmlClientbaseReaderTest {
    private final String clientsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
            "<clients xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
            "        xsi:noNamespaceSchemaLocation=\"ClientsXMLSchema.xsd\">\r\n" + 
            "        <client>\r\n" + 
            "                <name>user1</name>\r\n" + 
            "                <password>1234</password>\r\n" + 
            "        </client>\r\n" + 
            "        <client>\r\n" + 
            "                <name>user2</name>\r\n" + 
            "                <password>qwerty</password>\r\n" + 
            "        </client>\r\n" + 
            "</clients>";
    
    private final String notValidClientsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
            "<clients xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
            "        xsi:noNamespaceSchemaLocation=\"ClientsXMLSchema.xsd\">\r\n" + 
            "        <client>\r\n" + 
            "                <name>user1</name>\r\n" + 
            "                <password>1234</password>\r\n" + 
            "        </clien";
    
    public InputStream prepereInputStream() {
        return new ByteArrayInputStream(clientsXml.getBytes());
    }

    @Test
    public void getClientBaseTest() throws ClientbaseException {
        InputStream is = prepereInputStream();

        XmlClientbaseReader reader = new XmlClientbaseReader(is);

        Map<String, String> users = reader.getClientBase();

        int usersCount = users.size();
        assertEquals(2, usersCount);
    }
    
}
