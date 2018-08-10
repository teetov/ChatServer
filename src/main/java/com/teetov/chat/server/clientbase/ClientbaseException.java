package com.teetov.chat.server.clientbase;

@SuppressWarnings("serial")
public class ClientbaseException extends Exception {

    public ClientbaseException() {}

    public ClientbaseException(String message) {
        super(message);
    }

    public ClientbaseException(Throwable exc) {
        super(exc);
    }
    
    public ClientbaseException(String message, Throwable exc) {
        super(message, exc);
    }
}
