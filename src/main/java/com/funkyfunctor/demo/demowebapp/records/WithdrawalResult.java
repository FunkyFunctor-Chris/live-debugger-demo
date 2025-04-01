package com.funkyfunctor.demo.demowebapp.records;

public record WithdrawalResult(String accountId, long availableAmount, boolean isSuccess) {
}
