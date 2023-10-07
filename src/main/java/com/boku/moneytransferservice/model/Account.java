package com.boku.moneytransferservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Account {
    private UUID id;
    private String name;
    private BigDecimal balance;

    public void deposit(BigDecimal amount) {
        this.setBalance(balance.add(amount));
    }

    public void withdraw(BigDecimal amount) {
        this.setBalance(balance.subtract(amount));
    }
}
