package br.com.seuprojeto.antifraude;

import br.com.seuprojeto.antifraude.dto.TransactionRequest;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        partitions = 1,
        topics = {"transactions.analysis"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        brokerProperties = {
                "log.dir=target/kafka-test-logs",
                "log.cleaner.enable=false",}
)
@DirtiesContext // Garante que o contexto do Spring seja recriado entre os testes
class TransactionFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Fluxo completo: transação legítima deve ser aprovada")
    void shouldApproveTransaction_whenTransactionIsLegitimate() {
        TransactionRequest request = new TransactionRequest(
                "user-integration-01",
                new BigDecimal("500.00"),
                "São Paulo, BR",
                "177.42.0.1"
        );

        // 1. Envia o POST e verifica o 202
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/transactions", request, String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // 2. Extrai o ID da transação da resposta
        // (usa contains pois a resposta é um JSON com vários campos)
        assertThat(response.getBody()).contains("PENDING_ANALYSIS");

        // 3. Aguarda o Kafka processar e o status mudar (até 5 segundos)
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var transactions = transactionRepository
                            .findAll()
                            .stream()
                            .filter(t -> "user-integration-01".equals(t.getUserId()))
                            .findFirst();

                    assertThat(transactions).isPresent();
                    assertThat(transactions.get().getStatus())
                            .isEqualTo(TransactionStatus.APPROVED);
                });
    }

    @Test
    @DisplayName("Fluxo completo: transação com valor alto deve ser negada")
    void shouldDenyTransaction_whenAmountIsAboveLimit() {
        TransactionRequest request = new TransactionRequest(
                "user-integration-02",
                new BigDecimal("99999.00"),
                "São Paulo, BR",
                "177.42.0.1"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/transactions", request, String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var transactions = transactionRepository
                            .findAll()
                            .stream()
                            .filter(t -> "user-integration-02".equals(t.getUserId()))
                            .findFirst();

                    assertThat(transactions).isPresent();
                    assertThat(transactions.get().getStatus())
                            .isEqualTo(TransactionStatus.DENIED);
                });
    }
}