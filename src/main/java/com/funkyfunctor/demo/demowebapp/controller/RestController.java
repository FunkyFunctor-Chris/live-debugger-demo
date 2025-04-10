package com.funkyfunctor.demo.demowebapp.controller;

import com.funkyfunctor.demo.WithdrawalFraudProcessor;
import com.funkyfunctor.demo.demowebapp.FraudProcessor;
import com.funkyfunctor.demo.demowebapp.WithdrawalTransaction;
import com.funkyfunctor.demo.demowebapp.exceptions.NotEnoughFundsException;
import com.funkyfunctor.demo.demowebapp.exceptions.SuspiciousAmountException;
import com.funkyfunctor.demo.demowebapp.records.WithdrawalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vavr.control.Try;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    private final static Logger logger = LoggerFactory.getLogger(RestController.class);

    private static final FraudProcessor withdrawalFraudProcessor = new WithdrawalFraudProcessor();

    Map<String, Long> accounts = Collections.synchronizedMap(new HashMap<>());

    /**
     * This method handles the withdrawal transaction.
     * It checks if the amount is suspicious and if there are enough funds in the account.
     * If everything is ok, it processes the withdrawal.
     *
     * @param withdrawalTransaction The withdrawal transaction to process
     * @return A ResponseEntity containing the result of the withdrawal
     */
    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawalResult> withdrawEndpoint(@RequestBody WithdrawalTransaction withdrawalTransaction) {
        Try<WithdrawalResult> result = handleWithdrawal(withdrawalTransaction.accountId(), withdrawalTransaction.amount());

        if (result.isSuccess()) {
            logger.info("Withdrawal of {} cents from account {} was successful", withdrawalTransaction.amount(), withdrawalTransaction.accountId());
            return ResponseEntity.ok(result.get());
        } else {
            Throwable exception = result.getCause();
            if (exception instanceof NotEnoughFundsException) {
                logger.warn("Withdrawal of {} cents from account {} failed due to insufficient funds", withdrawalTransaction.amount(), withdrawalTransaction.accountId());
                return ResponseEntity.status(402) //HTTP Code for Payment Required
                        .body(new WithdrawalResult(withdrawalTransaction.accountId(), 0, false));
            } else if (exception instanceof SuspiciousAmountException) {
                logger.warn("Withdrawal of {} cents from account {} failed due to suspicious amount", withdrawalTransaction.amount(), withdrawalTransaction.accountId());
                return ResponseEntity.status(406) //HTTP Code for Not Acceptable
                        .body(new WithdrawalResult(withdrawalTransaction.accountId(), 0, false));
            } else {
                logger.error("Withdrawal of {} cents from account {} failed due to unexpected error", withdrawalTransaction.amount(), withdrawalTransaction.accountId(), exception);
                return ResponseEntity.status(500).body(new WithdrawalResult(withdrawalTransaction.accountId(), 0, false));
            }
        }
    }

    @GetMapping("/reset")
    public void resetAccounts() {
        logger.info("Accounts reset");
        accounts.clear();
    }

    @GetMapping("/long-running")
    public void longRunningRequest() {
        longRunningRequest(10); //10 seconds
    }

    @GetMapping("/long-running/{duration}")
    public void longRunningRequest(@PathVariable long duration) {
        Try<Void> result = Try.run(() -> Thread.sleep(duration * 1000));

        if (result.isSuccess()) {
            logger.debug("A long running request for {} seconds was successfully completed", duration);
        } else {
            logger.error("A long running request was interrupted");
        }
    }

    /**
     * This method handles the withdrawal transaction.
     * It checks if the amount is suspicious and if there are enough funds in the account.
     * If everything is ok, it processes the withdrawal.
     *
     * @param accountId The ID of the account to withdraw from
     * @param rawAmount The amount to withdraw
     * @return A Try object containing the result of the withdrawal
     */
    private Try<WithdrawalResult> handleWithdrawal(String accountId, int rawAmount) {
        return Try.of(() -> {
            int amount = Math.abs(rawAmount); //We want to make sure we are not dealing with negative amounts

            if (withdrawalFraudProcessor.isSuspiciousAmount(amount)) {
                throw new SuspiciousAmountException(accountId, amount);
            }

            //We process the transaction
            return processWithdrawal(accountId, amount);
        });
    }

    /**
     * This method processes the withdrawal transaction.
     * It checks if there are enough funds in the account and updates the account balance.
     *
     * @param accountId The ID of the account to withdraw from
     * @param amount    The amount to withdraw
     * @return A WithdrawalResult object containing the result of the withdrawal
     */
    private WithdrawalResult processWithdrawal(String accountId, int amount) {
        //We retrieve the account
        long availableAmount = getAvailableAmountOnAccount(accountId);

        //We check there is enough money in the account
        if (availableAmount < amount) {
            throw new NotEnoughFundsException(accountId, availableAmount, amount);
        } else {

            //We do the transaction
            long newAmount = availableAmount - amount;
            accounts.put(accountId, newAmount);

            return new WithdrawalResult(accountId, newAmount, true);
        }
    }

    /**
     * This method retrieves the available amount on the account.
     * In this case, we are simulating an account with a default amount of 1,000 dollars.
     *
     * @param accountId The ID of the account to check
     * @return The available amount on the account
     */
    private long getAvailableAmountOnAccount(String accountId) {
        return accounts.getOrDefault(accountId, withdrawalFraudProcessor.THRESHOLD() * 2L);
    }
}