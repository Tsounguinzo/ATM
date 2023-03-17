package com.mybank.atm.cashdispenser;

import java.math.BigDecimal;

public class CashDispenser implements ICashDispenser{
    private BigDecimal cashAvailable;

    public CashDispenser () {
        this(new BigDecimal("500.00"));
    }

    public CashDispenser (BigDecimal cashAvailable) {
        this.cashAvailable = cashAvailable;
    }

    public boolean hasEnoughCash (BigDecimal amount) {
        return amount.compareTo(cashAvailable) <= 0;
    }

    @Override
    public boolean dispenseCash (BigDecimal amount) {
        if (hasEnoughCash(amount)){
            System.out.printf("Dispensing $%.2f%n", amount);
            return true;
        }
        return false;
    }
}
