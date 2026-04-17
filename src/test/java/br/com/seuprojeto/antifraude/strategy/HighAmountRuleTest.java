package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HighAmountRuleTest {

    private HighAmountRule rule;

    @BeforeEach
    void setUp() {
        rule = new HighAmountRule();
    }

    private TransactionEvent buildEvent(BigDecimal amount) {
        return new TransactionEvent(1L, "user-123", amount, "São Paulo, BR", "177.0.0.1");
    }

    @Test
    @DisplayName("Deve aprovar transação com valor dentro do limite")
    void shouldApproveWhenAmountIsWithinLimit() {
        RuleResult result = rule.evaluate(buildEvent(new BigDecimal("9999.99")));

        assertThat(result.fraudDetected()).isFalse();
    }

    @Test
    @DisplayName("Deve negar transação com valor acima do limite")
    void shouldDenyWhenAmountExceedsLimit() {
        RuleResult result = rule.evaluate(buildEvent(new BigDecimal("10000.01")));

        assertThat(result.fraudDetected()).isTrue();
        assertThat(result.reason()).contains("excede o limite");
    }

    @Test
    @DisplayName("Deve aprovar transação com valor exatamente no limite")
    void shouldApproveWhenAmountIsExactlyAtLimit() {
        RuleResult result = rule.evaluate(buildEvent(new BigDecimal("10000.00")));

        assertThat(result.fraudDetected()).isFalse();
    }
}
