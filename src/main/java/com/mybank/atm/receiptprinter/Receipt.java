package com.mybank.atm.receiptprinter;

import com.mybank.atm.transaction.Transaction;

import java.util.List;

public class Receipt {
    private String accountNumber;
    private String accountBalance;
    private List<Transaction> transactions;

    public Receipt(String accountNumber, String accountBalance, List<Transaction> transactions) {
        this.accountNumber = accountNumber;
        this.accountBalance = accountBalance;
        this.transactions = transactions;
    }


    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
