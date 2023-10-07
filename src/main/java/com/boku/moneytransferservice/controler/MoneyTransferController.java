package com.boku.moneytransferservice.controler;

import com.boku.moneytransferservice.exception.MissingParameterException;
import com.boku.moneytransferservice.exception.ValidationException;
import com.boku.moneytransferservice.model.StatusResponse;
import com.boku.moneytransferservice.service.TransferService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyTransferController {
    private final TransferService transferService;

    public MoneyTransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    public void setupRoutes() {
        final var app = Javalin.create().start(8000);

        app.post("/transfer", this::handleTransferRequest);
        app.post("/withdrawal", this::handleWithdrawalRequest);
        app.get("/withdrawal/status/{withdrawalId}", this::handleWithdrawalStatusRequest);
        app.get("/account/{accountId}", this::handleGetAccountRequest);

        app.exception(MissingParameterException.class, (e, context) -> {
            context.status(400);
            context.json(new StatusResponse("error", e.getMessage(), null));
        });
        app.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(400);
            ctx.json(new StatusResponse("error", e.getMessage(), null));
        });
    }

    void handleTransferRequest(Context context) throws MissingParameterException, ValidationException
    {
        var senderAccountId = UUID.fromString(getQueryParam(context, "senderAccountId"));
        var receiverAccountId = UUID.fromString(getQueryParam(context, "receiverAccountId"));
        var amount = new BigDecimal(getQueryParam(context, "amount"));
        transferService.transferMoney(senderAccountId, receiverAccountId, amount);
        context.json(new StatusResponse("success", "Money transferred successfully.", null));
    }

    void handleWithdrawalRequest(Context context) throws MissingParameterException, ValidationException {
        var senderAccountId = UUID.fromString(getQueryParam(context, "senderAccountId"));
        var amount = new BigDecimal(getQueryParam(context, "amount"));
        var address = getQueryParam(context, "address");
        var withdrawalId = transferService.withdrawMoney(senderAccountId, address, amount);
        context.json(new StatusResponse("success", "Withdrawal request submitted.", withdrawalId));
    }

    void handleWithdrawalStatusRequest(Context context) {
        var id = UUID.fromString(context.pathParam("withdrawalId"));
        var state = transferService.getWithdrawalStatus(id);
        context.json(state);
    }

    void handleGetAccountRequest(Context context) {
        var accountId = UUID.fromString(context.pathParam("accountId"));
        var account = transferService.getAccountById(accountId);
        context.json(account);
    }

    private String getQueryParam(Context context, String paramName) throws MissingParameterException {
        var paramVal = context.queryParam(paramName);
        if (paramVal == null || paramVal.isEmpty()) {
            throw new MissingParameterException(paramName);
        }
        return paramVal;
    }
}
