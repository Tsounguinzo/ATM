package com.mybank.atm.bank;

import java.math.BigDecimal;

public interface IBank {
    boolean authenticateClient(String clientId, int pin);
    boolean isAccountLocked(String clientId) throws ClientNotFoundException;
    int getLoginAttempts(String clientId) throws ClientNotFoundException;
    void lockAccount(String clientId) throws ClientNotFoundException;
    Client getClient(String clientId) throws ClientNotFoundException;
    BigDecimal getAccountBalance(String clientId, AccountType type) throws AccountNotFoundException, ClientNotFoundException;
    void updateBalance(String clientId, AccountType type, BigDecimal newBalance) throws AccountNotFoundException, ClientNotFoundException;
    public void addClient(Client client);
    public void addAccount(Client client, Account account);
    String generateClientId();
}
