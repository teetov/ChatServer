package com.teetov.chat.server.clientbase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XmlClientbaseReaderFactory {
    static public XmlClientbaseReader getXmlClientbaseReader() throws ClientbaseException {
        InputStream is = createFileInputStream(getXmlPath());

        return new XmlClientbaseReader(is);
    }
    
    private static InputStream createFileInputStream(String path) throws ClientbaseException {
        InputStream is = null;

        try {
            is = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ClientbaseException("Client base not found",e);
        }

        return is;
    }
    
    private static String getXmlPath() {
        return XmlClientbaseReader.class.getResource("/xml/clients/ClientsXML.xml").toString();
    }
}
