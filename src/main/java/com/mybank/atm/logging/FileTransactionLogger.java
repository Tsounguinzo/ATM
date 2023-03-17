package com.mybank.atm.logging;

import com.mybank.atm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTransactionLogger implements ITransactionLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransactionLogger.class);
    public void logTransaction(Transaction transaction) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Transaction logged\n{}", transaction);
        }
    }
}
