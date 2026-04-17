package br.com.seuprojeto.antifraude;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.model.Transaction;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import br.com.seuprojeto.antifraude.service.FraudAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FraudAnalysisIntegrationTest {

    @Autowired
    private FraudAnalysisService fraudAnalysisService;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private SimpMessagingTemplate messagingTemplate; // WebSocket mockado

    private Long savedId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();

        Transaction transaction = new Transaction();
        transaction.setUserId("user-fraud-01");
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setLocation("São Paulo, BR");
        transaction.setIpAddress("177.0.0.1");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING_ANALYSIS);

        savedId = transactionRepository.save(transaction).getId();
    }

    @Test
    @DisplayName("Transação legítima deve ser APPROVED após análise")
    void shouldApproveWhenTransactionIsLegitimate() {
        TransactionEvent event = new TransactionEvent(
                savedId, "user-fraud-01", new BigDecimal("500.00"), "São Paulo, BR", "177.0.0.1"
        );

        fraudAnalysisService.analyze(event);

        var result = transactionRepository.findById(savedId);
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TransactionStatus.APPROVED);
    }

    @Test
    @DisplayName("Transação com valor alto deve ser DENIED após análise")
    void shouldDenyWhenAmountIsAboveLimit() {
        transactionRepository.findById(savedId).ifPresent(t -> {
            t.setAmount(new BigDecimal("99999.00"));
            transactionRepository.save(t);
        });

        TransactionEvent event = new TransactionEvent(
                savedId, "user-fraud-01", new BigDecimal("99999.00"), "São Paulo, BR", "177.0.0.1"
        );

        fraudAnalysisService.analyze(event);

        var result = transactionRepository.findById(savedId);
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TransactionStatus.DENIED);
    }
}