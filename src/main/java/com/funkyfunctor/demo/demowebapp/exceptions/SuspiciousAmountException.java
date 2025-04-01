package com.funkyfunctor.demo.demowebapp.exceptions;

public class SuspiciousAmountException extends RuntimeException {
    public SuspiciousAmountException(String accountId, int withdrawalAmount) {
        super(String.format("Suspicious withdrawal amount for account %s: %d", accountId, withdrawalAmount));
    }
}
