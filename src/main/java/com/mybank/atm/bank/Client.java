package com.mybank.atm.bank;

import java.util.Map;

public class Client {
    private String id;
    private String name;
    private int pin;
    private int loginAttempts;
    private boolean locked;
    private Map<AccountType, Account> accounts;

    public Client(String id, String name, int pin, Map<AccountType, Account> accounts) {
        this.id = id;
        this.name = name;
        this.pin = pin;
        this.loginAttempts = 0;
        this.locked = false;
        this.accounts = accounts;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<AccountType, Account> getAccounts() {
        return accounts;
    }

    public boolean checkPin(int pin) {
        return this.pin == pin;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void incrementLoginAttempts() {
        loginAttempts++;
    }

    public void resetLoginAttempts() {
        loginAttempts = 0;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
