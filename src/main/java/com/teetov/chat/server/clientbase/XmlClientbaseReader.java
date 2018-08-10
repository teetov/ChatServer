package com.teetov.chat.server.clientbase;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlClientbaseReader {

    private InputStream source;
    
    public XmlClientbaseReader(InputStream is) {
        source = is;
    }
    
    /**
     * Читает XML-файл со списком логинов и паролей клиентов;
     * 
     * 
     * @throws ClientbaseException
     */
    public Map<String, String> getClientBase() throws ClientbaseException {
        Map<String, String> users = new HashMap<>();
        DefaultHandler handler = getDefaultHandler(users);
        
        parse(handler);
        
        return users;
    }

    private void parse(DefaultHandler handler) throws ClientbaseException {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            parser.parse(source, handler);            
        } catch (Exception e) {
            throw new ClientbaseException("Client base not valid",e);
        }

    }

    private DefaultHandler getDefaultHandler(final Map<String, String> users) {
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
