package com.mybank.atm.bank;

public class ClientNotFoundException extends Exception{
    public ClientNotFoundException() {
        super("Client Not Found");
    }
}
