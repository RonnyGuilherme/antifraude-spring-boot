package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    private static final BigDecimal LIMIT = new BigDecimal("10000.00");

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.amount().compareTo(LIMIT) > 0) {
            return RuleResult.denied(
                    "Valor de R$ " + event.amount() + " excede o limite permitido de R$ " + LIMIT
            );
        }
        return RuleResult.approved();
    }
}
