# Wallet Service

Um serviço robusto para gerenciar fundos de usuários, oferecendo operações de depósito, saque e transferência entre carteiras.

## Sumário

- [Visão Geral](#visão-geral)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Execução da Aplicação](#execução-da-aplicação)
    - [Modo Desenvolvimento (H2)](#modo-desenvolvimento-h2)
    - [Modo Produção (PostgreSQL)](#modo-produção-postgresql)
    - [Modo Docker](#modo-docker)
- [Documentação da API](#documentação-da-api)
- [Testes](#testes)
- [Decisões de Projeto](#decisões-de-projeto)
- [Trade-offs e Limitações](#trade-offs-e-limitações)
- [Stack Tecnológica](#stack-tecnológica)
- [Modelo de Dados](#modelo-de-dados)
- [Integridade Transacional e Concorrência](#integridade-transacional-e-concorrência)
- [Tratamento de Erros e Validação](#tratamento-de-erros-e-validação)
- [Rastreabilidade e Auditoria](#rastreabilidade-e-auditoria)
- [Configuração Docker](#configuração-docker)
- [Notas de Documentação](#notas-de-documentação)

## Visão Geral

O Serviço de Carteira é um componente crítico para gestão de fundos de usuários com alta confiabilidade e rastreabilidade. Ele disponibiliza operações para:

- Criação de carteiras para usuários;
- Consulta de saldos atuais;
- Consulta de saldo histórico em um instante específico;
- Depósitos em carteiras;
- Saques de carteiras;
- Transferência de fundos entre carteiras.

O serviço é construído com Java 21 e Spring Boot 3.4.5 seguindo boas práticas de aplicações prontas para produção.

## Requisitos

- Java 21 ou superior;
- Maven 3.6 ou superior;
- Docker e Docker Compose (opcionais para execução containerizada).

## Instalação

1. Clone o repositório:

```bash
git clone https://github.com/JhonDias98/wallet-service.git
cd wallet-service
```

2. Compile o projeto:

```bash
mvn clean install
```

Essa etapa compila o código, executa os testes e empacota a aplicação em um arquivo JAR.

## Execução da Aplicação

### Modo Desenvolvimento (H2)

Para desenvolvimento, a aplicação utiliza um banco de dados em memória H2. Execute:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Alternativamente, você pode usar o script fornecido:

```bash
./run-dev.sh
```

A aplicação iniciará na porta padrão 8080. O console H2 ficará disponível em http://localhost:8080/h2-console com as credenciais:
- JDBC URL: `jdbc:h2:mem:walletdb`
- Username: `sa`
- Password: `password`

### Modo Produção (PostgreSQL)

Para produção, a aplicação utiliza PostgreSQL. Após compilar, execute:

```bash
java -jar -Dspring.profiles.active=prod target/wallet-service-1.0.0-SNAPSHOT.jar
```

1. **Pré-requisitos**:

    - Docker e Docker Compose instalados em seu sistema.
    - Se você estiver usando **Windows** ou **macOS**, certifique-se de que o **Docker Desktop** está instalado e em execução.
    - Se você estiver usando **Linux**, verifique se o Docker Engine e o Docker Compose estão instalados e se o daemon do Docker está em execução.
    - Caso esteja em **Linux** ou **WSL (Windows Subsystem for Linux)**, é necessário conceder permissão de execução ao script uma vez:

      ```bash
      chmod +x run-docker.sh
      ```
2. **Executar em Modo Desenvolvimento (Banco H2)**:

   ```bash
   ./run-docker.sh dev
   ```

   Isso irá:
    - Construir a imagem Docker da aplicação.
    - Iniciar o serviço Wallet Service com o perfil `dev` (usando H2).

   A aplicação estará disponível em http://localhost:8080  
   O Console H2 estará disponível em http://localhost:8080/h2-console  
   Configurações do H2:
    - JDBC URL: `jdbc:h2:mem:walletdb`
    - Username: `sa`
    - Password: `password`

3. **Executar em Modo Produção (Banco PostgreSQL)**:

    ```bash
   ./run-docker.sh prod
   ```

   Isso irá:
    - Construir a imagem Docker da aplicação.
    - Iniciar o banco de dados PostgreSQL.
    - Iniciar o serviço Wallet Service com o perfil `prod` (conectando ao PostgreSQL).

   A aplicação estará disponível em http://localhost:8080

   Para conectar-se ao banco de dados:
    - Host: `postgres`
    - Port: `5432`
    - Database: `walletdb`
    - Username: `postgres`
    - Password: `postgres`

## OpenAPI

Este projeto utiliza `SpringDoc OpenAPI` para gerar automaticamente a documentação interativa da API.

- Acessar Swagger UI - Ambiente de desenvolvimento
   ```bash
   http://localhost:8080/swagger-ui/index.html
   ```
- Especificação OpenAPI (JSON)
   ```bash
   http://localhost:8080/v3/api-docs
   ```
- ⚠️ Importante: O Swagger UI está desabilitado por padrão em produção por motivos de segurança.

## Documentação da API

### Criar carteira

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

### Obter saldo atual

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

### Obter histórico do saldo

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

### Depositar fundos

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

### Sacar fundos

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

### Transferir fundos

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

### Obter histórico de transações

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

## Testes

A aplicação inclui tanto testes unitários quanto testes de integração para garantir funcionalidade e confiabilidade.

### Executando Testes

```bash
mvn test
```

This will run all the tests and generate a test report.

### Cobertura de Testes

Os testes cobrem:
- Lógica da camada de serviço (unitários);
- Endpoints da API (integração);
- Casos de borda e tratamento de erros.

### Stack Tecnológica

- **Linguagem**: Java 21
- **Framework**: Spring Boot 3.4.5
- **Ferramenta de Build**: Maven
- **Banco de Dados**:
    - **Desenvolvimento/Testes**: H2 Database.
    - **Produção**: PostgreSQL.
- **ORM**: Spring Data JPA com Hibernate.
- **API**: Spring Web, SpringDoc OpenAPI 2.7.0.
- **Testes**: JUnit 5, Mockito, Spring Boot Test.
- **Containerização**: Docker e Docker Compose.

### Modelo de Dados

O modelo de dados consiste em duas entidades principais:

- **Wallet (Carteira)**:
    - `id` (UUID): Identificador único da carteira.
    - `userId` (Long): Identificador do usuário associado.
    - `balance` (BigDecimal): Saldo atual da carteira.
    - `version` (Long): Usado em lock otimista para evitar problemas de concorrência.
    - `createdAt` (Instant): Data de criação.
    - `updatedAt` (Instant): Data da última atualização.

- **Transaction (Transação)**:
    - `id` (UUID): Identificador único da transação.
    - `walletId` (UUID): Chave estrangeira para a carteira.
    - `type` (Enum: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT): Tipo da operação.
    - `amount` (BigDecimal): Valor da operação.
    - `timestamp` (Instant): Momento da transação.
    - `status` (Enum: PENDING, COMPLETED, FAILED): Status da transação.
    - `referenceId` (UUID): Para vincular transações relacionadas (ex.: transferências).
    - `description` (String): Detalhes adicionais.
    - `balanceAfter` (BigDecimal): Saldo após a operação.

Cada transação é registrada explicitamente, garantindo trilha de auditoria e rastreabilidade. O saldo histórico pode ser calculado consultando a última transação antes de determinado instante.