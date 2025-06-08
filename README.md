# Wallet Service

A robust wallet service that manages user funds, providing operations for deposit, withdrawal, and transfer of funds between users.

## Development time

Approximately 7 hours and 20 minutes.

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Installation](#installation)
- [Running the Application](#running-the-application)
  - [Development Mode (H2)](#development-mode-h2)
  - [Production Mode (PostgreSQL)](#production-mode-postgresql)
  - [Docker Mode](#docker-mode)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Design Decisions](#design-decisions)
- [Trade-offs and Limitations](#trade-offs-and-limitations)
- [Code Quality and Analysis](#code-quality-and-analysis)

## Overview

This Wallet Service is a critical component designed to manage user funds with high reliability and traceability. It provides a comprehensive set of operations for wallet management, including:

- Creating wallets for users
- Querying current wallet balances
- Querying historical wallet balances at specific points in time
- Depositing funds into wallets
- Withdrawing funds from wallets
- Transferring funds between wallets

The service is built with Java 17 and Spring Boot 3.5.0, following best practices for production-ready applications.

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose (optional, for containerized deployment)

## Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/JhonDias98/wallet-service.git
    cd wallet-service
    ```

2. Build the application:

    ```bash
    mvn clean install
    ```

This will compile the code, run the tests, and package the application into a JAR file.

## Running the Application

### Development Mode (H2)

For development purposes, the application is configured to use an in-memory H2 database. You can run it directly:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Alternatively, you can use the provided script:

```bash
./run-dev.sh
```

The application will start on port 8080 by default. You can access the H2 console at http://localhost:8080/h2-console with the following credentials:
- JDBC URL: `jdbc:h2:mem:walletdb`
- Username: `sa`
- Password: `password`

### Production Mode (PostgreSQL)

For production deployment, the application is configured to use a PostgreSQL database. You can run it directly:

```bash
java -jar -Dspring.profiles.active=prod target/wallet-service-1.0.0-SNAPSHOT.jar
```

### Docker Mode

The application can be easily run using Docker and Docker Compose, which will set up the application and the appropriate database based on the selected profile.

1. **Prerequisites**:

   - Docker and Docker Compose installed on your system.
   - If you're using **Windows** or **macOS**, ensure **Docker Desktop** is installed and running.
   - If you're using **Linux**, make sure the Docker Engine and Docker Compose are installed and the Docker daemon is running.
   - If you're on **Linux** or **WSL (Windows Subsystem for Linux)**, you need to grant execution permission to the script once:
  
     ```bash
     chmod +x run-docker.sh
     ```
2. **Run in Development Mode (H2 Database)**:

   ```bash
   ./run-docker.sh dev
   ```

   This will:
    - Build the application Docker image.
    - Start the Wallet Service application with the `dev` profile (using H2).

   The application will be available at http://localhost:8080
   The H2 Console will be available at http://localhost:8080/h2-console
   H2 Console settings:
    - JDBC URL: `jdbc:h2:mem:walletdb`
    - Username: `sa`
    - Password: `password`

3. **Run in Production Mode (PostgreSQL Database)**:

    ```bash
   ./run-docker.sh prod
   ```

   This will:
    - Build the application Docker image.
    - Start PostgreSQL database.
    - Start the Wallet Service application with the `prod` profile (connecting to PostgreSQL).

   The application will be available at http://localhost:8080

   To connect to the database:
    - Host: `postgres`
    - Port: `5432`
    - Database: `walletdb`
    - Username: `postgres`
    - Password: `postgres`

## OpenAPI

This project uses `SpringDoc OpenAPI` to automatically generate interactive API documentation.

- Accessing Swagger UI - Development environment
   ```bash
   http://localhost:8080/swagger-ui/index.html
   ```
- OpenAPI Specification (JSON)
   ```bash
   http://localhost:8080/v3/api-docs
   ```
- ⚠️ Important: Swagger UI is disabled by default in production for security reasons.

## API Documentation

### Create Wallet

```
POST /api/wallets
```

Request body:
```json
{
  "userId": 1
}
```

Response (201 Created):
```json
{
  "id": "124de968-a954-41dd-80e1-6feb6f4779e7",
  "userId": 1,
  "balance": 0,
  "createdAt": "2025-06-08T01:39:35.741760Z",
  "updatedAt": "2025-06-08T01:39:35.741760Z"
}
```

### Get Current Balance

```
GET /api/wallets/{walletId}/balance
```

Response (200 OK):
```json
{
  "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
  "balance": 1000.0000,
  "timestamp": "2025-06-08T01:41:33.181336900Z"
}
```

### Get Historical Balance

```
GET /api/wallets/{walletId}/balance/history?timestamp=2025-06-09T00:00:00Z
```

Response (200 OK):
```json
{
  "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
  "balance": 1000.0000,
  "timestamp": "2025-06-08T19:33:16.846240Z"
}
```

### Deposit Funds

```
POST /api/wallets/{walletId}/deposit
```

Request body:
```json
{
  "amount": 1000,
  "description": "Salary deposit"
}
```

Response (200 OK):
```json
{
  "id": "12103de9-13c9-4446-a5af-b16e42496c24",
  "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
  "type": "DEPOSIT",
  "amount": 1000,
  "timestamp": "2025-06-08T01:40:22.289564Z",
  "status": "COMPLETED",
  "description": "Salary deposit",
  "balanceAfter": 1000.0000
}
```

### Withdraw Funds

```
POST /api/wallets/{walletId}/withdraw
```

Request body:
```json
{
  "amount": 100.50,
  "description": "ATM withdrawal"
}
```

Response (200 OK):
```json
{
  "id": "2014bd91-7889-4035-8e0f-94119f8cc45f",
  "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
  "type": "WITHDRAWAL",
  "amount": 100.50,
  "timestamp": "2025-06-08T01:44:02.970516Z",
  "status": "COMPLETED",
  "description": "ATM withdrawal",
  "balanceAfter": 899.5000
}
```

### Transfer Funds

```
POST /api/wallets/transfer
```

Request body:
```json
{
  "sourceWalletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
  "destinationWalletId": "0047733a-a041-41e8-af15-75f1b967466f",
  "amount": 200,
  "description": "Payment for services"
}
```

Response (200 OK):
```json
[
  {
    "id": "cf0ae6a7-64c9-4a0a-a240-8ce546c074c6",
    "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
    "type": "TRANSFER_OUT",
    "amount": 200,
    "status": "COMPLETED",
    "referenceId": "9e082c5f-33cd-4441-bf4e-22c6c369588d",
    "description": "Payment for services",
    "balanceAfter": 699.5000
  },
  {
    "id": "086b3a2b-0a04-49cc-97b7-e0c76c5dae47",
    "walletId": "0047733a-a041-41e8-af15-75f1b967466f",
    "type": "TRANSFER_IN",
    "amount": 200,
    "status": "COMPLETED",
    "referenceId": "9e082c5f-33cd-4441-bf4e-22c6c369588d",
    "description": "Payment for services",
    "balanceAfter": 200.0000
  }
]
```

### Get Transaction History

```
GET /api/wallets/{walletId}/transactions?page=0&size=20
```

Response (200 OK):
```json
[
  {
    "id": "cf0ae6a7-64c9-4a0a-a240-8ce546c074c6",
    "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
    "type": "TRANSFER_OUT",
    "amount": 200.0000,
    "timestamp": "2025-06-08T01:45:12.137856Z",
    "status": "COMPLETED",
    "referenceId": "9e082c5f-33cd-4441-bf4e-22c6c369588d",
    "description": "Payment for services",
    "balanceAfter": 699.5000
  },
  {
    "id": "2014bd91-7889-4035-8e0f-94119f8cc45f",
    "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
    "type": "WITHDRAWAL",
    "amount": 100.5000,
    "timestamp": "2025-06-08T01:44:02.970516Z",
    "status": "COMPLETED",
    "description": "ATM withdrawal",
    "balanceAfter": 899.5000
  },
  {
    "id": "12103de9-13c9-4446-a5af-b16e42496c24",
    "walletId": "124de968-a954-41dd-80e1-6feb6f4779e7",
    "type": "DEPOSIT",
    "amount": 1000.0000,
    "timestamp": "2025-06-08T01:40:22.289564Z",
    "status": "COMPLETED",
    "description": "Salary deposit",
    "balanceAfter": 1000.0000
  }
]
```

## Testing

The application includes both unit tests and integration tests to ensure functionality and reliability.

### Running Tests

```bash
mvn test
```

This will run all the tests and generate a test report.

### Test Coverage

The tests cover:
- Service layer logic (unit tests)
- API endpoints (integration tests)
- Edge cases and error handling

## Design Decisions

### Architecture Overview

The service is designed as a standalone Spring Boot application that can be deployed as a microservice. This architecture was chosen to ensure:

- **High Availability**: The service can be scaled horizontally to handle increased load.
- **Fault Tolerance**: The service can recover from failures gracefully.
- **Scalability**: The service can be scaled independently of other components.

### Technology Stack

- **Language**: Java 17.
- **Framework**: Spring Boot 3.5.0.
- **Build Tool**: Maven
- **Database**:
  - **Development/Testing**: H2 Database.
  - **Production**: PostgreSQL.
- **ORM**: Spring Data JPA with Hibernate.
- **API**: Spring Web, SpringDoc OpenAPI 2.7.0.
- **Testing**: JUnit 5, Mockito, Spring Boot Test.
- **Containerization**: Docker and Docker Compose.

### Data Model Design

The data model consists of two main entities:

- **Wallet Entity**: Represents a user's wallet, containing:
  - `id` (UUID): Unique identifier for the wallet.
  - `userId` (Long): Identifier for the associated user.
  - `balance` (BigDecimal): Current balance of the wallet.
  - `version` (Long): For optimistic locking to prevent concurrent update issues.
  - `createdAt` (Instant): Timestamp of wallet creation.
  - `updatedAt` (Instant): Timestamp of last update.

- **Transaction Entity**: Represents a single monetary operation, containing:
  - `id` (UUID): Unique identifier for the transaction.
  - `walletId` (UUID): Foreign key to the `Wallet` involved in the transaction.
  - `type` (Enum: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT): Type of operation.
  - `amount` (BigDecimal): Amount of money involved in the transaction.
  - `timestamp` (Instant): Timestamp of the transaction.
  - `status` (Enum: PENDING, COMPLETED, FAILED): Status of the transaction.
  - `referenceId` (UUID): For linking related transactions (e.g., transfers).
  - `description` (String): Additional details about the transaction.
  - `balanceAfter` (BigDecimal): Wallet balance after this transaction (for historical balance queries).

The explicit `Transaction` entity ensures that every single operation that alters a wallet's balance is recorded, providing the necessary audit trail and traceability. Historical balance can be derived by finding the latest transaction before a specific point in time.

### Transactional Integrity and Concurrency

- **ACID Transactions**: All operations that modify wallet balances (deposit, withdraw, transfer) are wrapped in database transactions to ensure Atomicity, Consistency, Isolation, and Durability.
- **Optimistic Locking**: For concurrent updates to wallet balances, optimistic locking (using a version field in the Wallet entity) is implemented to prevent lost updates.
- **Idempotency**: API endpoints are designed to be idempotent where possible, especially for operations like deposits, to prevent duplicate processing if a request is retried.

### Error Handling and Validation

- **Custom Exceptions**: Custom exceptions are defined for business-specific errors (e.g., `InsufficientFundsException`, `WalletNotFoundException`).
- **Global Exception Handler**: A `@ControllerAdvice` handles exceptions globally and returns consistent, meaningful error responses.
- **Input Validation**: Spring's `@Valid` annotation and JSR 303/380 (Bean Validation) are used for validating incoming request payloads.

### Traceability and Auditing

- **Transaction Entity**: As detailed in the data model, the `Transaction` entity is the primary mechanism for traceability. Every balance change corresponds to a recorded transaction.
- **Logging**: Comprehensive logging is implemented using SLF4J/Logback to capture key events, request/response details, and errors.
- **Unique Identifiers**: UUIDs are used for transaction IDs to ensure global uniqueness and ease of tracing across distributed systems.

### Docker Configuration

The application includes Docker configuration for easy deployment and environment consistency:

- **Multi-stage Dockerfile**: Uses a multi-stage build process to create a lightweight and efficient Docker image.
- **Docker Compose**: Provides a complete development environment with PostgreSQL.
- **Volume Persistence**: Database data is persisted using Docker volumes.
- **Environment Variables**: Configuration is managed through environment variables for flexibility.

## Trade-offs and Limitations

### Database Versioning

This implementation uses `ddl-auto=update` in the development environment and `ddl-auto=validate` in production. This ensures that schema changes are applied automatically during development while preventing accidental changes in production. However, the project does not yet adopt a database versioning tool such as Flyway or Liquibase. In a production-grade system, managing schema changes through migration scripts improves traceability, enables rollback strategies, and ensures safe and consistent deployment across environments.

### Database Credentials and Configuration Management

The current configuration includes database credentials and connection details directly within the application property files. This approach, while straightforward for local development, poses a security risk if used in production. A more secure and scalable practice is to externalize sensitive configuration, using environment variables or secret management solutions (e.g., AWS Secrets Manager), ensuring credentials are protected and rotated as needed without requiring code changes.

### Authentication and Authorization

This implementation focuses on the core wallet service logic and does not include authentication and authorization mechanisms. In a real production environment, this service would be protected by an authentication and authorization mechanism (e.g., OAuth2, JWT) provided by an API Gateway or a dedicated identity service.

### Distributed Transactions

The current implementation handles transfers between wallets within the same database transaction. In a fully distributed microservices architecture, a more sophisticated approach like the Saga pattern might be needed to handle distributed transactions across multiple services.

### Caching

The current implementation does not include a caching layer. For very high read loads on current balances, a caching layer (e.g., Redis) could be introduced. However, given the critical nature of monetary data, cache invalidation strategies would need careful design.

### Monitoring and Alerting

The service does not include built-in monitoring and alerting capabilities. In a production environment, it would be integrated with monitoring tools (e.g., Prometheus, Grafana) to track performance metrics, health, and generate alerts on anomalies.

---

This wallet service implementation provides a solid foundation for managing user funds with high reliability and traceability. It can be extended and enhanced based on specific business requirements and operational needs.

---

## Code Quality and Analysis

### JaCoCo Code Coverage

The project is configured with JaCoCo for code coverage analysis. JaCoCo provides detailed reports on test coverage, helping ensure code quality and identifying areas that need additional testing.

#### Generating Coverage Reports

To generate a code coverage report:

```bash
mvn clean test jacoco:report
```

This command will:
1. Clean the project
2. Run all tests
3. Generate a JaCoCo coverage report

The coverage report will be available at:
- **HTML Report**: `target/site/jacoco/index.html`
- **XML Report**: `target/site/jacoco/jacoco.xml`
- **CSV Report**: `target/site/jacoco/jacoco.csv`

#### Coverage Thresholds

The project is configured with minimum coverage thresholds:
- **Line Coverage**: 70% minimum
- **Branch Coverage**: 60% minimum

If the coverage falls below these thresholds, the build will fail. You can adjust these thresholds in the `pom.xml` file under the JaCoCo plugin configuration.

#### Viewing Coverage Reports

1. After running the coverage command, open the HTML report:
   ```bash
   open target/site/jacoco/index.html
   ```
   Or navigate to the file in your browser.

2. The report provides:
    - Overall coverage statistics
    - Package-level coverage breakdown
    - Class-level coverage details
    - Line-by-line coverage highlighting

### SonarQube Code Quality Analysis

SonarQube is integrated for comprehensive code quality analysis, including code smells, bugs, vulnerabilities, and technical debt assessment.

#### Starting SonarQube (Manual)

To start SonarQube manually using Docker:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
```

#### Accessing SonarQube Interface

Once SonarQube is running, you can access the web interface at:
- **URL**: http://localhost:9000
- **Default Username**: `admin`
- **Default Password**: `admin`

**First-time Setup**:
1. Navigate to http://localhost:9000
2. Log in with `admin/admin`
3. You'll be prompted to change the default password
4. [Follow the setup wizard to configure your instance](https://docs.sonarqube.org/latest/setup/get-started-2-minutes/)

#### Running Code Analysis

To analyze your code with SonarQube:

```bash
mvn clean test jacoco:report sonar:sonar \
  -Dsonar.projectKey=wallet-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN_HERE
```

This command will:
1. Clean the project
2. Run all tests
3. Generate JaCoCo coverage report
4. Send the analysis results to SonarQube

#### Viewing Analysis Results

1. After running the analysis, go to http://localhost:9000
2. You'll see your project listed on the dashboard
3. Click on the project to view detailed analysis results including:
    - **Overview**: Summary of issues, coverage, and duplications
    - **Issues**: Detailed list of bugs, vulnerabilities, and code smells
    - **Measures**: Metrics like lines of code, complexity, and coverage
    - **Code**: Source code browser with issue annotations
    - **Activity**: History of analysis runs and trends

#### Understanding SonarQube Metrics

- **Bugs**: Issues that represent mistakes in the code
- **Vulnerabilities**: Security-related issues
- **Code Smells**: Maintainability issues
- **Coverage**: Test coverage percentage
- **Duplications**: Duplicated code blocks
- **Technical Debt**: Estimated time to fix all maintainability issues

#### Quality Gates

SonarQube uses Quality Gates to define the criteria for code quality. The default Quality Gate includes:
- No new bugs
- No new vulnerabilities
- No new security hotspots
- Coverage on new code ≥ 80%
- Duplicated lines on new code < 3%


### Best Practices for Code Quality

1. **Regular Analysis**: Run code analysis regularly during development
2. **Fix Issues Early**: Address issues as they're identified rather than accumulating technical debt
3. **Review Coverage**: Aim for high test coverage, especially for critical business logic
4. **Monitor Trends**: Use SonarQube's historical data to track code quality trends
5. **Team Standards**: Establish team standards for acceptable quality gate criteria
