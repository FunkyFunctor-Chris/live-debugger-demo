package com.funkyfunctor.demo.demowebapp;

public record WithdrawalTransaction(
        String accountId, //Id of the account to withdraw from
        int amount //Amount to withdraw in cents
) {
}