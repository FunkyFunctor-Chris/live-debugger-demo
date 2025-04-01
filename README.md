# Live Debugger Demo

## How to run the project
### Locally
```bash
./mvnw spring-boot:run
```

### Package the project
```bash
./mvnw clean package
```

## Samples
<details>

### Sample of working CURL

We withdraw $1

```bash
curl --location 'http://localhost:8080/withdraw' \
--header 'Content-Type: application/json' \
--data '{
    "accountId" : "Test123",
    "amount": 100
}'
```

### Sample of not enough money on the account
We need to run this command 3 times to make sure there is not enough money on the account.

```bash
curl --location 'http://localhost:8080/withdraw' \
--header 'Content-Type: application/json' \
--data '{
    "accountId" : "Test123",
    "amount": 9999999
}'
```

### Sample of reaching the threshold for suspicious activity

```bash
curl --location 'http://localhost:8080/withdraw' \
--header 'Content-Type: application/json' \
--data '{
    "accountId" : "Test123",
    "amount": 10000001
}'
```

## Sample of illegal CURL that still goes through

For some reason, this value adds some money to the account instead of taking it away.

```bash
curl --location 'http://localhost:8080/withdraw' \
--header 'Content-Type: application/json' \
--data '{
    "accountId" : "Test123",
    "amount": -2147483648
}'
```
</details>