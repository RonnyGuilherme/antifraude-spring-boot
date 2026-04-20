package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HighAmountRuleTest {

    private HighAmountRule rule;

    @BeforeEach
    void setUp() {
        rule = new HighAmountRule();
    }

    @ParameterizedTest(name = "valor={0} → fraude={1}")
    @CsvSource({
            "9999.99,  false",  // abaixo do limite
            "10000.00, false",  // exatamente no limite (não excede)
            "10000.01, true",   // um centavo acima
            "50000.00, true",   // muito acima
            "0.01,     false",  // valor mínimo
    })
    @DisplayName("Deve avaliar corretamente o limite de valor")
    void shouldEvaluateAmountLimit(BigDecimal amount, boolean expectedFraud) {
        TransactionEvent event = new TransactionEvent(
                1L, "user-test", amount, "São Paulo, BR", "177.0.0.1"
        );

        RuleResult result = rule.evaluate(event);

        assertThat(result.fraudDetected()).isEqualTo(expectedFraud);
        if (expectedFraud) {
            assertThat(result.reason()).contains("excede o limite");
        }
    }
}