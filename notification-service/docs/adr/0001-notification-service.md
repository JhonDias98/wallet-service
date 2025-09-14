# 0001 - Criação do Notification Service

## Contexto
Necessário enviar notificações a partir de eventos de transações recebidos via RabbitMQ.

## Decisão
Criado serviço dedicado com arquitetura em camadas (domain, application, infrastructure) e instrumentação com Micrometer/OpenTelemetry.

## Consequências
- Novas dependências para mensageria e métricas.
- Monitoramento exposto via `/actuator`.