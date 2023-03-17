package com.mybank.atm.bank;

public class AccountNotFoundException extends Exception{
    public AccountNotFoundException() {
        super("Account not found.");
    }
}
