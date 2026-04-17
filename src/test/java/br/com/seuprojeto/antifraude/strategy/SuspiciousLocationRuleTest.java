package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SuspiciousLocationRuleTest {

    private SuspiciousLocationRule rule;

    @BeforeEach
    void setUp() {
        rule = new SuspiciousLocationRule();
    }

    private TransactionEvent buildEvent(String location) {
        return new TransactionEvent(1L, "user-123", new BigDecimal("500.00"), location, "10.0.0.1");
    }

    @Test
    @DisplayName("Deve aprovar transação de localização segura")
    void shouldApproveWhenLocationIsSafe() {
        RuleResult result = rule.evaluate(buildEvent("São Paulo, BR"));

        assertThat(result.fraudDetected()).isFalse();
    }

    @Test
    @DisplayName("Deve negar transação de país de risco")
    void shouldDenyWhenLocationIsHighRisk() {
        RuleResult result = rule.evaluate(buildEvent("Pyongyang, KP"));

        assertThat(result.fraudDetected()).isTrue();
        assertThat(result.reason()).contains("risco");
    }

    @Test
    @DisplayName("Deve aprovar quando localização é nula")
    void shouldApproveWhenLocationIsNull() {
        RuleResult result = rule.evaluate(buildEvent(null));

        assertThat(result.fraudDetected()).isFalse();
    }
}
