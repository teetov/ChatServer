package com.teetov.chat.server.clientbase;

import java.util.Map;

public class ClientValidatorFactory {

    public static ClientValidator getClientValidator() throws ClientbaseException {

        XmlClientbaseReader reader = XmlClientbaseReaderFactory.getXmlClientbaseReader();
        Map<String, String> clients = reader.getClientBase();
        return new ClientValidator(clients);
    }
}
