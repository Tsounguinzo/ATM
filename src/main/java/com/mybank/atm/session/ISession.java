package com.mybank.atm.session;

public interface ISession {
    void createSession(String userId);
    String getClientId() throws SessionNotFoundException;
    void endSession() throws SessionNotFoundException, SessionExpiredException;
}
