package com.mybank.atm.logging;

import com.mybank.atm.transaction.Transaction;

public interface ITransactionLogger {
    void logTransaction(Transaction transaction);
}
