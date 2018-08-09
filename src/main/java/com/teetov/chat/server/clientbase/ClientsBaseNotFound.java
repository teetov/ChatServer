package com.teetov.chat.server.clientbase;

public class ClientsBaseNotFound extends Exception {

    private static final long serialVersionUID = 1L;

    public ClientsBaseNotFound() {}    

    public ClientsBaseNotFound(Throwable exc) {
        super(exc);
    }
}
