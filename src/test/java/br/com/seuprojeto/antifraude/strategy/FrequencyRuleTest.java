package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrequencyRuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FrequencyRule rule;

    private TransactionEvent event;

    @BeforeEach
    void setUp() {
        event = new TransactionEvent(
                1L, "user-123", new BigDecimal("300.00"), "SP, BR", "1.2.3.4"
        );
    }

    @ParameterizedTest(name = "contagem={0} → fraude={1}")
    @CsvSource({
            "0, false",   // nenhuma transação anterior
            "3, false",   // abaixo do limite
            "5, false",   // exatamente no limite (não excede)
            "6, true",    // um acima do limite
            "10, true",   // muito acima
            "100, true",  // caso extremo
    })
    @DisplayName("Deve avaliar corretamente a frequência de transações")
    void shouldEvaluateTransactionFrequency(long count, boolean expectedFraud) {
        when(transactionRepository.countRecentTransactions(
                eq("user-123"), any(LocalDateTime.class))
        ).thenReturn(count);

        RuleResult result = rule.evaluate(event);

        assertThat(result.fraudDetected()).isEqualTo(expectedFraud);
        if (expectedFraud) {
            assertThat(result.reason()).contains("user-123");
        }
    }
}