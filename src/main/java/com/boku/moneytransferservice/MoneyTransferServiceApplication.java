package com.boku.moneytransferservice;


import com.boku.moneytransferservice.controler.MoneyTransferController;
import com.boku.moneytransferservice.service.TransferService;
import com.boku.moneytransferservice.service.WithdrawalServiceStub;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyTransferServiceApplication {
	private final TransferService transferService;

	public MoneyTransferServiceApplication() {
		var withdrawalService = new WithdrawalServiceStub();
		transferService = new TransferService(withdrawalService);
	}

	public void init() {
		var controller = new MoneyTransferController(transferService);
		controller.setupRoutes();

		var acc1 = transferService.createAccount(UUID.fromString("12345678-abcd-abcd-1234-000000000001"), "Boku User 1", new BigDecimal("1000"));
		var acc2 = transferService.createAccount(UUID.fromString("12345678-abcd-abcd-1234-000000000002"), "Boku User 2", new BigDecimal("500"));

		System.out.println("Account added, " + acc1.getId());
		System.out.println("Account added, " + acc2.getId());
	}

	public static void main(String[] args) {
		var app = new MoneyTransferServiceApplication();
		app.init();
	}
}
