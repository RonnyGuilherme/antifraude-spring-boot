package br.com.seuprojeto.antifraude.service;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.model.Transaction;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import br.com.seuprojeto.antifraude.strategy.FraudRule;
import br.com.seuprojeto.antifraude.strategy.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudAnalysisService {

    private final List<FraudRule> rules;  // Spring injeta todas as FraudRule automaticamente
    private final TransactionRepository transactionRepository;

    public void analyze(TransactionEvent event) {
        log.info("Iniciando análise da transação ID: {}", event.transactionId());

        for (FraudRule rule : rules) {
            RuleResult result = rule.evaluate(event);

            if (result.fraudDetected()) {
                updateStatus(event.transactionId(), TransactionStatus.DENIED);
                log.warn("Transação ID: {} NEGADA. Motivo: {}",
                        event.transactionId(), result.reason());
                return; // Para na primeira regra que detectar fraude
            }
        }

        updateStatus(event.transactionId(), TransactionStatus.APPROVED);
        log.info("Transação ID: {} APROVADA.", event.transactionId());
    }

    private void updateStatus(Long transactionId, TransactionStatus status) {
        transactionRepository.findById(transactionId).ifPresent(transaction -> {
            transaction.setStatus(status);
            transactionRepository.save(transaction);
        });
    }
}