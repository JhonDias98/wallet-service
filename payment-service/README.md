# Payment Service

Serviço responsável por processar depósitos e saques. Integra-se a um provedor externo fictício utilizando Resilience4j e publica o evento `payment.completed` no RabbitMQ.

## Executando localmente

```bash
mvn -pl payment-service spring-boot:run
```

A aplicação inicia na porta `8082` e utiliza H2 em memória.

## Executando com Docker

```bash
docker build -t payment-service -f payment-service/Dockerfile .
docker compose up payment-service
```

O container expõe a porta `8082` e espera conexões com PostgreSQL e RabbitMQ definidos no `docker-compose.yml`.