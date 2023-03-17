package com.mybank.atm;

import com.mybank.atm.bank.AccountType;
import com.mybank.atm.receiptprinter.Receipt;

import java.math.BigDecimal;

public interface IATM {
    void login(String userId, int PIN) throws ATMException;
    Receipt deposit(BigDecimal amount, AccountType type) throws ATMException;
    Receipt withdraw(BigDecimal amount, AccountType type) throws ATMException;
    Receipt InternalTransfer(AccountType fromAccount, AccountType toAccount, BigDecimal amount) throws ATMException;
    Receipt ExternalTransfer(AccountType fromAccount, AccountType toAccount, String toClientId, BigDecimal amount) throws ATMException;
    void viewBalance(AccountType type) throws ATMException;
    void endSession() throws ATMException;
}