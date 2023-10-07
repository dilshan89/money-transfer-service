package com.boku.moneytransferservice.service;

import com.boku.moneytransferservice.exception.ValidationException;
import com.boku.moneytransferservice.service.WithdrawalService.WithdrawalState;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class TransferServiceTest {

    private TransferService transferService;
    private WithdrawalService withdrawalService;

    @Before
    public void setUp() {
        withdrawalService = mock(WithdrawalService.class);
        transferService = new TransferService(withdrawalService);
    }

    @Test
    public void testTransferMoney() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        var receiverAccountId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");

        var senderAccount = transferService.createAccount(senderAccountId, "Sender", initialBalance);
        var receiverAccount = transferService.createAccount(receiverAccountId, "Receiver", initialBalance);

        transferService.transferMoney(senderAccountId, receiverAccountId, new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("150.00"), receiverAccount.getBalance());
    }

    @Test(expected = ValidationException.class)
    public void testTransferMoneyInvalidAmount() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        var receiverAccountId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");

        transferService.createAccount(senderAccountId, "Sender", initialBalance);
        transferService.createAccount(receiverAccountId, "Receiver", initialBalance);
        transferService.transferMoney(senderAccountId, receiverAccountId, new BigDecimal("-50.00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferMoneyInsufficientBalance() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        var receiverAccountId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");

        transferService.createAccount(senderAccountId, "Sender", initialBalance);
        transferService.createAccount(receiverAccountId, "Receiver", initialBalance);
        transferService.transferMoney(senderAccountId, receiverAccountId, new BigDecimal("150.00"));
    }

    @Test
    public void testWithdrawMoney() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        var senderAccount = transferService.createAccount(senderAccountId, "Sender", new BigDecimal("100.00"));
        when(withdrawalService.getRequestState(any())).thenReturn(WithdrawalService.WithdrawalState.COMPLETED);

        var withdrawalId = transferService.withdrawMoney(senderAccountId, "Address", new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), senderAccount.getBalance());
        assertEquals(WithdrawalService.WithdrawalState.COMPLETED, transferService.getWithdrawalStatus(withdrawalId));
    }

    @Test(expected = ValidationException.class)
    public void testWithdrawMoneyInvalidAmount() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        transferService.createAccount(senderAccountId, "Sender", new BigDecimal("100.00"));
        transferService.withdrawMoney(senderAccountId, "Address", new BigDecimal("-50.00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithdrawMoneyInsufficientBalance() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        transferService.createAccount(senderAccountId, "Sender", new BigDecimal("100.00"));
        transferService.withdrawMoney(senderAccountId, "Address", new BigDecimal("150.00"));
    }

    @Test
    public void testGetWithdrawalStatus() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        transferService.createAccount(senderAccountId, "Sender", new BigDecimal("100.00"));
        var withdrawalId = transferService.withdrawMoney(senderAccountId, "Address", new BigDecimal("50.00"));

        when(withdrawalService.getRequestState(any())).thenReturn(WithdrawalState.PROCESSING);
        assertEquals(WithdrawalState.PROCESSING, transferService.getWithdrawalStatus(withdrawalId));
        when(withdrawalService.getRequestState(any())).thenReturn(WithdrawalState.COMPLETED);
        assertEquals(WithdrawalState.COMPLETED, transferService.getWithdrawalStatus(withdrawalId));
    }

    @Test
    public void testCreateAccount() {
        var accountId = UUID.randomUUID();
        var account = transferService.createAccount(accountId, "Test Account", new BigDecimal("100.00"));

        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals("Test Account", account.getName());
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    public void testGetAccountById() {
        var accountId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");
        transferService.createAccount(accountId, "Test Account", initialBalance);
        var retrievedAccount = transferService.getAccountById(accountId);

        assertNotNull(retrievedAccount);
        assertEquals(accountId, retrievedAccount.getId());
        assertEquals("Test Account", retrievedAccount.getName());
        assertEquals(initialBalance, retrievedAccount.getBalance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAccountByIdNotFound() {
        var accountId = UUID.randomUUID();
        transferService.getAccountById(accountId);
    }

    @Test
    public void testCheckWithdrawalStatus() throws ValidationException {
        var senderAccountId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");
        var senderAccount = transferService.createAccount(senderAccountId, "Sender", initialBalance);
        var withdrawalId = transferService.withdrawMoney(senderAccountId, "Address", new BigDecimal("50.00"));
        when(withdrawalService.getRequestState(any())).thenReturn(WithdrawalState.PROCESSING);

        WithdrawalService.WithdrawalState withdrawalState = transferService.getWithdrawalStatus(withdrawalId);
        assertEquals(WithdrawalState.PROCESSING, withdrawalState);

        when(withdrawalService.getRequestState(any())).thenReturn(WithdrawalService.WithdrawalState.COMPLETED);
        withdrawalState = transferService.getWithdrawalStatus(withdrawalId);

        assertEquals(WithdrawalService.WithdrawalState.COMPLETED, withdrawalState);
        assertEquals(new BigDecimal("50.00"), senderAccount.getBalance());
    }

    @Test
    public void testScheduledTask() throws InterruptedException, ValidationException {
        var senderAccountId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");

        var senderAccount = transferService.createAccount(senderAccountId, "Sender", initialBalance);
        var withdrawalId = transferService.withdrawMoney(senderAccountId, "Address", new BigDecimal("50.00"));

        // Simulate time passing by waiting for the task to execute
        TimeUnit.SECONDS.sleep(2);
        when(withdrawalService.getRequestState(any())).thenReturn(WithdrawalService.WithdrawalState.COMPLETED);
        WithdrawalService.WithdrawalState withdrawalState = transferService.getWithdrawalStatus(withdrawalId);

        assertEquals(WithdrawalService.WithdrawalState.COMPLETED, withdrawalState);
        assertEquals(new BigDecimal("50.00"), senderAccount.getBalance());
    }
}
