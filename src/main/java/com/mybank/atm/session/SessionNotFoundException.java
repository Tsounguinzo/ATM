package com.mybank.atm.session;

public class SessionNotFoundException extends RuntimeException{
    public SessionNotFoundException() {
        super("Session not found.");
    }
}
