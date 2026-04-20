package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SuspiciousLocationRule implements FraudRule {

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of(
            "RU", "KP", "IR", "SY"
    );

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.location() == null || event.location().isBlank()) {
            return RuleResult.approved();
        }

        // Extrai o código do país: último token após vírgula ou espaço
        // Ex: "São Paulo, BR" → "BR" | "Pyongyang KP" → "KP" | "kp" → "KP"
        String location = event.location().trim().toUpperCase();
        String countryCode = location.contains(",")
                ? location.substring(location.lastIndexOf(',') + 1).trim()
                : location.substring(location.lastIndexOf(' ') + 1).trim();

        if (HIGH_RISK_COUNTRIES.contains(countryCode)) {
            return RuleResult.denied(
                    "Transação originada de localização de risco: " + event.location()
            );
        }
        return RuleResult.approved();
    }
}