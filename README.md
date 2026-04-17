# 🛡️ Sistema Antifraude — Spring Boot

API REST para análise assíncrona de fraudes em transações financeiras, usando arquitetura orientada a eventos e processamento desacoplado via Kafka.
## 🚀 Tecnologias
- Java 21 + Spring Boot 3.x
- Apache Kafka (mensageria assíncrona)
- Spring Data JPA + H2 Database
- OpenAPI/Swagger (documentação)
- Padrão de projeto Strategy (motor de regras)
- JUnit 5 + Mockito + Awaitility (testes integrados)

## 🧩 Arquitetura
POST /transactions
│
▼
API (202 Accepted)         ← responde imediatamente, sem bloquear
│
▼
Apache Kafka               ← desacopla a análise da entrada
│
▼
FraudAnalysisService       ← itera sobre as regras via Strategy
│
┌────┴────┐
│ Rules   │  HighAmountRule · SuspiciousLocationRule · FrequencyRule
└────┬────┘
│
▼
Banco de dados (H2)        ← status atualizado para APPROVED ou DENIED
│
▼
WebSocket /topic/transactions/{id}  ← cliente notificado em tempo real

## 🔍 Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/transactions` | Submete transação para análise (retorna 202) |
| GET | `/transactions/{id}` | Consulta status de uma transação |
| GET | `/transactions` | Histórico paginado com filtros por `userId` e `status` |
| WS | `/ws` → `/topic/transactions/{id}` | Notificação em tempo real do resultado |

Documentação interativa: `http://localhost:8080/swagger-ui/index.html`


## Características
- Processamento assíncrono (API nunca bloqueia)
- Baixo acoplamento entre API e regras — tudo passado por eventos
- Escalabilidade horizontal via múltiplos consumers

## Regras de antifraude implementadas
- **Valor alto:** nega transações acima de R$ 10.000
- **Localização suspeita:** nega transações de países de risco
- **Frequência:** nega mais de 5 transações do mesmo usuário em 10 minutos
- **As regras são plugáveis via Strategy, permitindo adicionar novas regras sem alterar a estrutura central**

## ▶️ Como rodar

### Pré-requisitos
- Java 21
- Docker Desktop

### Subir o Kafka
```bash
docker compose up -d
```

### Rodar a aplicação
```bash
./mvnw spring-boot:run
```

### Acessar o Swagger
http://localhost:8080/swagger-ui/index.html

### Rodar os testes
```bash
./mvnw test
```

## 🔍 Consultar transações via API
- Consultar o status de uma transação específica
```bash
curl http://localhost:8080/transactions/1
```

- Listar todas as transações (paginado)
```bash
curl "http://localhost:8080/transactions?page=0&size=10"
```
- Filtrar transações negadas de um usuário

```bash
curl "http://localhost:8080/transactions?userId=user-123&status=DENIED"
```
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)

