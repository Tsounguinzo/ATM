package com.mybank.atm.bank;

import java.math.BigDecimal;

/**
 * This class represent a bank account
 */
public class Account {

    private String clientId;
    private AccountType type;
    private String name;
    private String currency;
    private BigDecimal balance;

    /**
     * Used to create a new bank account
     * @param clientId the id of the account owner
     * @param type type of account
     * @param name name given to the account
     * @param currency currency used in the account
     * @param balance account balance
     */
    public Account(String clientId, AccountType type, String name, String currency, BigDecimal balance) {
        this.clientId = clientId;
        this.type = type;
        this.name = name;
        this.currency = currency;
        this.balance = balance;
    }

    /**
     * Gets the account identification number
     * @return the account identification number
     */
    public static int getAccountId(AccountType type) {
        return switch (type){
            case CHECKING_ACCOUNT -> 1;
            case SAVINGS_ACCOUNT -> 2;
            case INVESTMENT_ACCOUNT -> 3;
            case CREDIT_ACCOUNT -> 4;
        };
    }

    /**
     * Gets the account user id
     * @return the user id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the type of account
     * @return the type of account
     */
    public AccountType getType() {
        return type;
    }

    /**
     * @return the name of the account
     */
    public String getName() {
        return name;
    }

    /**
     * @return the currency used by the account
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return the account balance
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Set the account balance
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
