package com.mybank.atm.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private static String transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private String fromClientId;
    private int fromAccountId;
    private String toClientId;
    private int toAccountId;
    private LocalDateTime timestamp;

    public Transaction(TransactionType type, BigDecimal amount, String fromClientId, int fromAccountId) {
        this(type,amount,fromClientId,fromAccountId,"000000000",0);
    }

    public Transaction(TransactionType type, BigDecimal amount, String fromClientId, int fromAccountId, String toClientId, int toAccountId) {
        this.type = type;
        this.amount = amount;
        this.fromClientId = fromClientId;
        this.fromAccountId = fromAccountId;
        this.toClientId = toClientId;
        this.toAccountId = toAccountId;
        this.timestamp = LocalDateTime.now();
        transactionId = String.valueOf(UUID.randomUUID());
    }

    public Transaction(TransactionType type, BigDecimal amount, String fromClientId, int fromAccountId, int toAccountId) {
        this(type,amount,fromClientId,fromAccountId,"000000000",toAccountId);
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getFromClientId() {
        return fromClientId;
    }

    public int getFromAccountId() {
        return fromAccountId;
    }

    public String getToClientId() {
        return toClientId;
    }

    public int getToAccountId() {
        return toAccountId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction ID: ").append(transactionId).append("\n");
        sb.append("Type: ").append(type).append("\n");
        sb.append("Amount: ").append(amount).append("\n");
        sb.append("From Client ID: ").append(fromClientId).append("\n");
        sb.append("From Account ID: ").append(fromAccountId).append("\n");

        if (type == TransactionType.TRANSFER) {
            sb.append("To Client ID: ").append(toClientId).append("\n");
            sb.append("To Account ID: ").append(toAccountId).append("\n");
        }

        sb.append("Timestamp: ").append(timestamp).append("\n");
        return sb.toString();
    }
}
