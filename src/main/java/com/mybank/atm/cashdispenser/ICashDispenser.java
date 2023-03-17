package com.mybank.atm.cashdispenser;

import java.math.BigDecimal;

public interface ICashDispenser {
    boolean hasEnoughCash(BigDecimal amount);
    boolean dispenseCash(BigDecimal amount);
}
