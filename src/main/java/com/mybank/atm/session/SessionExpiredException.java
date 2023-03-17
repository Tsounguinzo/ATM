package com.mybank.atm.session;

public class SessionExpiredException extends RuntimeException{
    public SessionExpiredException() {
        super("Session expired.");
    }
}
