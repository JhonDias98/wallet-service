# User Service

Serviço responsável pelo cadastro e autenticação de usuários.

## Endpoints

- `POST /users` - cria um usuário.
- `POST /login` - realiza login e retorna um JWT.

## Executando localmente

```bash
mvn spring-boot:run -pl user-service
```

## Testes

```bash
mvn test -pl user-service
```

## Docker

```bash
docker build -t user-service -f user-service/Dockerfile .
```

O serviço expõe métricas em `/actuator/prometheus` e publica eventos `user.created` no RabbitMQ.