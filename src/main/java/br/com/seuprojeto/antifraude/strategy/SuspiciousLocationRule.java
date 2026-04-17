package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SuspiciousLocationRule implements FraudRule {

    private static final Set<String> HIGH_RISK_LOCATIONS = Set.of(
            "RU", "KP", "IR", "SY"
    );

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.location() == null) {
            return RuleResult.approved();
        }

        boolean isRisky = HIGH_RISK_LOCATIONS.stream()
                .anyMatch(country -> event.location().toUpperCase().contains(country));

        if (isRisky) {
            return RuleResult.denied(
                    "Transação originada de localização de risco: " + event.location()
            );
        }
        return RuleResult.approved();
    }
}