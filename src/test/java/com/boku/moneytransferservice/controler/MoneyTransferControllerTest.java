package com.boku.moneytransferservice.controler;

import com.boku.moneytransferservice.exception.MissingParameterException;
import com.boku.moneytransferservice.exception.ValidationException;
import com.boku.moneytransferservice.model.Account;
import com.boku.moneytransferservice.model.StatusResponse;
import com.boku.moneytransferservice.service.TransferService;
import com.boku.moneytransferservice.service.WithdrawalService.WithdrawalState;
import io.javalin.http.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static com.boku.moneytransferservice.service.WithdrawalService.WithdrawalState.PROCESSING;
import static org.mockito.Mockito.*;

public class MoneyTransferControllerTest {

    @Mock
    private TransferService moneyTransferService;
    @Mock
    private Context context;
    private MoneyTransferController moneyTransferController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        moneyTransferController = new MoneyTransferController(moneyTransferService);
    }

    @Test
    public void testHandleTransferRequestValidParams() throws ValidationException, MissingParameterException {
        var senderAccountId = UUID.randomUUID();
        var receiverAccountId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(100.0);

        when(context.queryParam("senderAccountId")).thenReturn(senderAccountId.toString());
        when(context.queryParam("receiverAccountId")).thenReturn(receiverAccountId.toString());
        when(context.queryParam("amount")).thenReturn(amount.toString());

        moneyTransferController.handleTransferRequest(context);
        verify(moneyTransferService).transferMoney(senderAccountId, receiverAccountId, amount);
        verify(context).json( new StatusResponse("success", "Money transferred successfully.", null));
    }

    @Test(expected = MissingParameterException.class)
    public void testHandleTransferRequestInvalidParams() throws ValidationException, MissingParameterException {
        when(context.queryParam(anyString())).thenReturn(null);
        moneyTransferController.handleTransferRequest(context);
        verify(moneyTransferService, never()).transferMoney(any(), any(), any());
    }

    @Test
    public void testHandleWithdrawalRequestValidParams() throws ValidationException, MissingParameterException {
        var senderAccountId = UUID.randomUUID();
        var address = "1234-5678";
        var amount = BigDecimal.valueOf(50.0);

        when(context.queryParam("senderAccountId")).thenReturn(senderAccountId.toString());
        when(context.queryParam("address")).thenReturn(address);
        when(context.queryParam("amount")).thenReturn(amount.toString());
        UUID withdrawalId = UUID.randomUUID();
        when(moneyTransferService.withdrawMoney(senderAccountId, address, amount)).thenReturn(withdrawalId);

        moneyTransferController.handleWithdrawalRequest(context);
        verify(moneyTransferService).withdrawMoney(senderAccountId, address, amount);
        verify(context).json(new StatusResponse("success", "Withdrawal request submitted.", withdrawalId));
    }

    @Test(expected = MissingParameterException.class)
    public void testHandleWithdrawalRequestInvalidParams() throws ValidationException, MissingParameterException {
        when(context.queryParam(anyString())).thenReturn(null);
        moneyTransferController.handleWithdrawalRequest(context);
        verify(moneyTransferService, never()).withdrawMoney(any(), any(), any());
    }

    @Test
    public void testHandleWithdrawalStatusRequestValidId() {
        var withdrawalId = UUID.randomUUID();
        when(context.pathParam("withdrawalId")).thenReturn(withdrawalId.toString());
        WithdrawalState withdrawalState = PROCESSING;
        when(moneyTransferService.getWithdrawalStatus(withdrawalId)).thenReturn(withdrawalState);

        moneyTransferController.handleWithdrawalStatusRequest(context);
        verify(moneyTransferService).getWithdrawalStatus(withdrawalId);
        verify(context).json(withdrawalState);
    }

    @Test
    public void testHandleGetAccountRequestValidId() {
        var accountId = UUID.randomUUID();
        when(context.pathParam("accountId")).thenReturn(accountId.toString());
        var account = new Account(accountId, "John Doe", BigDecimal.valueOf(500.0));
        when(moneyTransferService.getAccountById(accountId)).thenReturn(account);

        moneyTransferController.handleGetAccountRequest(context);
        verify(moneyTransferService).getAccountById(accountId);
        verify(context).json(account);
    }
}
