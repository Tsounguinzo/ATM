package com.mybank.atm.bank;


import com.mybank.atm.ATM;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class represent a concrete bank that implements the bank interface
 */
public class Bank implements IBank{
    private Map<String, Client> clients;

    /**
     * Used to create a new Bank
     */
    public Bank() {
        clients = new HashMap<>();
    }

    /**
     * authentication f the user
     * @param clientId the client ID
     * @param pin the client secret PIN
     * @return true if the authentication was successful, false otherwise
     */
    @Override
    public boolean authenticateClient(String clientId, int pin) {
        Client client = clients.get(clientId);
        if (client == null) {
            return false;
        }

        if (client.isLocked()) {
            return false;
        }

        if (client.checkPin(pin)) {
            client.resetLoginAttempts();
            return true;
        } else {
            client.incrementLoginAttempts();
            if (client.getLoginAttempts() >= ATM.MAX_LOGIN_ATTEMPTS) {
                try {
                    lockAccount(clientId);
                } catch (ClientNotFoundException ignored){}
            }
            return false;
        }
    }

    @Override
    public boolean isAccountLocked(String clientId) throws ClientNotFoundException {
        Client client = clients.get(clientId);
        if (client == null) {
            throw new ClientNotFoundException();
        }

        return client.isLocked();
    }

    @Override
    public int getLoginAttempts(String clientId) throws ClientNotFoundException {
        Client client = clients.get(clientId);
        if (client == null) {
            throw new ClientNotFoundException();
        }

        return client.getLoginAttempts();
    }

    @Override
    public void lockAccount(String clientId) throws ClientNotFoundException {
        Client client = clients.get(clientId);
        if (client == null) {
            throw new ClientNotFoundException();
        }

        client.setLocked(true);
    }

    @Override
    public Client getClient(String clientId) throws ClientNotFoundException {
        Client client = clients.get(clientId);
        if (client == null) {
            throw new ClientNotFoundException();
        }

        return clients.get(clientId);
    }

    @Override
    public BigDecimal getAccountBalance(String clientId, AccountType type) throws AccountNotFoundException, ClientNotFoundException {
        Client client = getClient(clientId);
        Account account = client.getAccounts().get(type);
        if (account == null || !clientId.equals(account.getClientId())) {
            throw new AccountNotFoundException();
        }

        return account.getBalance();
    }

    @Override
    public void updateBalance(String clientId, AccountType type, BigDecimal newBalance) throws AccountNotFoundException, ClientNotFoundException {
        Client client = getClient(clientId);
        Account account = client.getAccounts().get(type);
        if (account == null || !clientId.equals(account.getClientId())) {
            throw new AccountNotFoundException();
        }

        account.setBalance(newBalance);
    }

    @Override
    public void addClient(Client client) {
        clients.put(client.getId(), client);
    }

    @Override
    public void addAccount(Client client, Account account) {
        client.getAccounts().put(account.getType(), account);
    }

    @Override
    public String generateClientId() {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString().replace("-", "");
        return id.substring(0, 9);
    }
}
