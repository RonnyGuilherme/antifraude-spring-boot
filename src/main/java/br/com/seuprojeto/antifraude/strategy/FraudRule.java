package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;

public interface FraudRule {
    RuleResult evaluate(TransactionEvent event);
}