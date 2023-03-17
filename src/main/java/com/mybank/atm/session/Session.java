package com.mybank.atm.session;

import java.time.LocalDateTime;

public class Session implements ISession{
    private String ClientId;
    private LocalDateTime lastAccessTime;

    public void createSession(String ClientId) {
        this.ClientId = ClientId;
        this.lastAccessTime = LocalDateTime.now();
    }

    public String getClientId() throws SessionNotFoundException {
        if (ClientId == null) {
            throw new SessionNotFoundException();
        }

        return ClientId;
    }

    public void endSession() throws SessionNotFoundException, SessionExpiredException {
        if (ClientId == null) {
            throw new SessionNotFoundException();
        }

        if (LocalDateTime.now().isAfter(lastAccessTime.plusMinutes(2))) {
            throw new SessionExpiredException();
        }

        ClientId = null;
        lastAccessTime = null;
    }
}
