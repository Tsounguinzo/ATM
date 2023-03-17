package com.mybank.atm.security;

public interface IBiometricScanner {
    boolean authenticateUser(String userId);
}
