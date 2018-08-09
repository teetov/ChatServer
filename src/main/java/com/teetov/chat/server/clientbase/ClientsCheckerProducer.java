package com.teetov.chat.server.clientbase;

public class ClientsCheckerProducer {
    
    private ClientsChecker checker;
    
    public synchronized ClientsChecker getClientsChecker() throws ClientsBaseNotFound {
        if(checker == null) 
            checker = new ClientsCheckerXML();
        return checker;
    }

}
