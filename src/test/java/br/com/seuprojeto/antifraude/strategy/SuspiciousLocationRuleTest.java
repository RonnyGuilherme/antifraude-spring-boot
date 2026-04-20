package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SuspiciousLocationRuleTest {

    private SuspiciousLocationRule rule;

    @BeforeEach
    void setUp() {
        rule = new SuspiciousLocationRule();
    }

    @ParameterizedTest(name = "location={0} → fraude={1}")
    @CsvSource(delimiter = '|', value = {
            "São Paulo, BR        | false",
            "Rio de Janeiro, BR   | false",
            "Pyongyang, KP        | true",
            "Tehran, IR           | true",
            "Damascus, SY         | true",
            "Moscow, RU           | true",
            "kp                   | true",
    })
    @DisplayName("Deve avaliar corretamente a localização")
    void shouldEvaluateLocation(String location, boolean expectedFraud) {
        TransactionEvent event = new TransactionEvent(
                1L, "user-test", new BigDecimal("500.00"), location.trim(), "10.0.0.1"
        );

        RuleResult result = rule.evaluate(event);

        assertThat(result.fraudDetected()).isEqualTo(expectedFraud);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Deve aprovar quando localização é nula")
    void shouldApproveWhenLocationIsNull(String location) {
        TransactionEvent event = new TransactionEvent(
                1L, "user-test", new BigDecimal("500.00"), location, "10.0.0.1"
        );

        assertThat(rule.evaluate(event).fraudDetected()).isFalse();
    }
}