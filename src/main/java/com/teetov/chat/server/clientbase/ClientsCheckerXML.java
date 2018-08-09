package com.teetov.chat.server.clientbase;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ClientsCheckerXML implements ClientsChecker{
    private Map<String, String> users = new HashMap<>();
    
    private String pathXml = ClientsCheckerXML.class.getResource("/xml/clients/ClientsXML.xml").toString();
    
    public ClientsCheckerXML() throws ClientsBaseNotFound {            
        readXML();
    }
    
    @Override
    public boolean isCorrectPassword(String name, String password) {
        String pass = users.get(name);
        if(pass != null) {
            if(pass.equals(password))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean isCorrectName(String name) {
        return users.containsKey(name);
    }
    
    /**
     * Читает XML-файл со списком логинов и паролей клиентов в Map<String, String> users;
     * 
     * @throws ClientsBaseNotFound
     */
    private void readXML() throws ClientsBaseNotFound {
        
        DefaultHandler handler = getDefaultHandler();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            
            parser.parse(pathXml, handler);            
        } catch (Exception e) {
            throw new ClientsBaseNotFound(e);
        } 
    }

    private DefaultHandler getDefaultHandler() {
        return new DefaultHandler() {

            String name;
            String password;

            boolean startName = false;
            boolean startPassword = false;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
                if(qName.equals("name")) 
                    startName = true;
                if(qName.equals("password")) 
                    startPassword = true;
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if(startName) {
                    name = String.valueOf(ch, start, length);
                    startName = false;
                }
                if(startPassword) {
                    password = String.valueOf(ch, start, length);
                    startPassword = false;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                users.put(name, password);
            }

        };
    }
}
