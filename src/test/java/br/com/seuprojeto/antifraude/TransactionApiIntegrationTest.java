package br.com.seuprojeto.antifraude;

import br.com.seuprojeto.antifraude.dto.TransactionRequest;
import br.com.seuprojeto.antifraude.event.producer.TransactionProducer;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private TransactionProducer transactionProducer; // Kafka mockado — sem broker real

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /transactions deve retornar 202 e salvar com PENDING_ANALYSIS")
    void shouldReturn202AndSaveAsPending() {
        TransactionRequest request = new TransactionRequest(
                "user-api-01",
                new BigDecimal("500.00"),
                "São Paulo, BR",
                "177.42.0.1"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/transactions", request, String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).contains("PENDING_ANALYSIS");

        var saved = transactionRepository.findAll().stream()
                .filter(t -> "user-api-01".equals(t.getUserId()))
                .findFirst();

        assertThat(saved).isPresent();
        assertThat(saved.get().getStatus()).isEqualTo(TransactionStatus.PENDING_ANALYSIS);
        assertThat(saved.get().getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("GET /transactions/{id} deve retornar 404 para ID inexistente")
    void shouldReturn404WhenTransactionNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/transactions/99999", String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /transactions deve retornar 400 para dados inválidos")
    void shouldReturn400WhenDataIsInvalid() {
        TransactionRequest request = new TransactionRequest(
                "",
                new BigDecimal("-10"),
                null,
                null
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/transactions", request, String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}