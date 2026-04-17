package br.com.seuprojeto.antifraude.service;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.dto.TransactionStatusUpdate;
import br.com.seuprojeto.antifraude.model.Transaction;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import br.com.seuprojeto.antifraude.strategy.FraudRule;
import br.com.seuprojeto.antifraude.strategy.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudAnalysisService {

    private final List<FraudRule> rules;
    private final TransactionRepository transactionRepository;
    private final SimpMessagingTemplate messagingTemplate;  // Bean do WebSocket

    public void analyze(TransactionEvent event) {
        log.info("Iniciando análise da transação ID: {}", event.transactionId());

        for (FraudRule rule : rules) {
            RuleResult result = rule.evaluate(event);
            if (result.fraudDetected()) {
                updateAndNotify(event, TransactionStatus.DENIED, result.reason());
                log.warn("Transação ID: {} NEGADA. Motivo: {}", event.transactionId(), result.reason());
                return;
            }
        }

        updateAndNotify(event, TransactionStatus.APPROVED, "Transação aprovada com sucesso");
        log.info("Transação ID: {} APROVADA.", event.transactionId());
    }

    private void updateAndNotify(TransactionEvent event, TransactionStatus status, String message) {
        // 1. Atualiza no banco
        transactionRepository.findById(event.transactionId()).ifPresent(transaction -> {
            transaction.setStatus(status);
            transactionRepository.save(transaction);
        });

        // 2. Publica no tópico WebSocket específico da transação
        TransactionStatusUpdate update = new TransactionStatusUpdate(
                event.transactionId(),
                event.userId(),
                status,
                message,
                LocalDateTime.now()
        );

        // Tópico geral: qualquer cliente pode ouvir todas as atualizações
        messagingTemplate.convertAndSend("/topic/transactions", update);

        // Tópico individual: cliente ouve só a transação que submeteu
        messagingTemplate.convertAndSend("/topic/transactions/" + event.transactionId(), update);
    }
}