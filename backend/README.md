# Shipping Management Backend

Spring Boot backend for the shipping management course design project.

## Stack

- Java 17
- Spring Boot 3.3.5
- MyBatis
- MySQL
- JUnit 5

## Database

```text
jdbc:mysql://localhost:3306/shipping_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
username: gavin
password: 456123
```

The course design intentionally stores user passwords as plain text. Login compares the submitted password with the database value directly.

## Tests

The current tests are lightweight unit tests for the core rules:

- Plain text username/password login
- Settlement amount and status calculation
- Transport order creation validation
- Transport status transitions

Run after Maven is available:

```powershell
mvn test
```
