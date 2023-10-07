package com.boku.moneytransferservice.model;

import com.boku.moneytransferservice.service.WithdrawalService.WithdrawalId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class Withdrawal {
    private WithdrawalId withdrawalId;
    private Account senderAccount;
    private BigDecimal amount;
}
