# Money Transfer Service (RESTFUL API)

## Assumptions and Decisions

1. Single currency support, No currency validations.
2. Account validations to add later, for now consider all accounts valid.
3. WithdrawalService made public after discussion, for avoid making wrapper classes of it.
4. **T** type **amount** to be taken as BigDecimal as it is the most suitable datatype.
5. Use Javalin library for RESTFUL support as it is lightweight for a standalone application
6. Use lombok for Getter, Setter and AllArgumentConstructor

## Instructions to local run
- Use JDK 17+
- Just build and run the "MoneyTransferServiceApplication"
- Main application will be open at: http://localhost:8000/
- Have added two default Accounts (12345678-abcd-abcd-1234-000000000001, 12345678-abcd-abcd-1234-000000000001) for testing purpose upon app run

# Supported APIs
### Transfer money from one account to another
- POST: http://localhost:8000/transfer (Query params: senderAccountId, receiverAccountId, amount)
- eg: http://localhost:8000/transfer?senderAccountId=12345678-abcd-abcd-1234-000000000001&receiverAccountId=12345678-abcd-abcd-1234-000000000002&amount=100

### Money withdrawal request from an account
- POST: http://localhost:8000/withdrawal (Query params: senderAccountId, address, amount)
- eg: http://localhost:8000/withdrawal?senderAccountId=12345678-abcd-abcd-1234-000000000001&address=aaa&amount=200

### Money withdrawal status query
- GET: http://localhost:8000/withdrawal/status/{withdrawalId}
- eg: http://localhost:8000/withdrawal/status/0037c351-5fb5-42d6-af8f-f8cea2f42a87

### Check Account details
- GET: http://localhost:8000/account/{accountId}
- eg: http://localhost:8000/account/12345678-abcd-abcd-1234-000000000001