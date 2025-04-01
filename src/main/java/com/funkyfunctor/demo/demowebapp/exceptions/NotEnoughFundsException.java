package com.funkyfunctor.demo.demowebapp.exceptions;

public class NotEnoughFundsException extends RuntimeException {
    public NotEnoughFundsException(String accountId, long availableAmount, int withdrawalAmount) {
        super(String.format("Not enough funds for account %s: available %d, requested %d", accountId, availableAmount, withdrawalAmount));
    }
}
