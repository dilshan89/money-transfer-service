package com.boku.moneytransferservice.service;

import com.boku.moneytransferservice.exception.ValidationException;
import com.boku.moneytransferservice.model.Withdrawal;
import com.boku.moneytransferservice.service.WithdrawalService.WithdrawalId;
import com.boku.moneytransferservice.service.WithdrawalService.WithdrawalState;
import com.boku.moneytransferservice.model.Account;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

public class TransferService {
    private final ConcurrentMap<UUID, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<WithdrawalId, Withdrawal> withdrawals = new ConcurrentHashMap<>();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final WithdrawalService withdrawalService;

    public TransferService(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
        init();
    }

    private void init() {
        // Schedule the task to run every second - Check withdrawal statuses and update accounts
        executor.scheduleAtFixedRate(this::checkWithdrawalStatus, 0, 1, TimeUnit.SECONDS);
    }

    private void checkWithdrawalStatus() {
        withdrawals.forEach(this::checkWithdrawalStatus);
    }

    private synchronized WithdrawalState checkWithdrawalStatus(WithdrawalId withdrawalId, Withdrawal withdrawal) {
        var status = withdrawalService.getRequestState(withdrawalId);
        if (withdrawal == null) return status;

        /*
          Once withdrawal request sent in, deduct amount from the balance
          Then if operation is FAILED, it will be added back
          Or else COMPLETED, Nothing to do as we initially deducted the amount
         */
        if (status == WithdrawalService.WithdrawalState.FAILED) {
            var finalBalance = withdrawal.getSenderAccount().getBalance().add(withdrawal.getAmount());
            withdrawal.getSenderAccount().setBalance(finalBalance);
            withdrawals.remove(withdrawalId);
        } else if (status == WithdrawalService.WithdrawalState.COMPLETED) {
            withdrawals.remove(withdrawalId);
        }
        return status;
    }

    public synchronized void transferMoney(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) throws ValidationException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException("Value must be greater than zero");

        var senderAccount = getAccountById(senderAccountId);
        var receiverAccount = getAccountById(receiverAccountId);

        // Accounts validations can be added here
        if (senderAccount.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient balance for transfer");

        senderAccount.withdraw(amount);
        receiverAccount.deposit(amount);
    }

    public synchronized UUID withdrawMoney(UUID senderAccountId, String address, BigDecimal amount) throws ValidationException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException("Value must be greater than zero");

        var senderAccount = getAccountById(senderAccountId);

        // Account validations can be added here
        if (senderAccount.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient balance for withdrawal");

        senderAccount.withdraw(amount);

        var withdrawalId = new WithdrawalId(UUID.randomUUID());
        withdrawals.put(withdrawalId, new Withdrawal(withdrawalId, senderAccount, amount));
        withdrawalService.requestWithdrawal(withdrawalId, new WithdrawalService.Address(address), amount);

        return withdrawalId.value();
    }

    public synchronized WithdrawalState getWithdrawalStatus(UUID id) {
        var withdrawalId = new WithdrawalId(id);
        var withdrawal = withdrawals.get(withdrawalId);
        return checkWithdrawalStatus(withdrawalId, withdrawal);
    }

    public synchronized Account createAccount(UUID id, String name, BigDecimal initialBalance) {
        var account = new Account(id, name, initialBalance);
        accounts.put(id, account);
        return account;
    }

    public synchronized Account getAccountById(UUID accountId) {
        var account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found with given Id: " + accountId);
        }
        return account;
    }
}
