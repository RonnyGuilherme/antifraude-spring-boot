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
POST /transactions → API (202 Accepted) → Kafka → Consumer → Regras de Fraude → Banco

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