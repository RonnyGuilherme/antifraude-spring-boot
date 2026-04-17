package br.com.seuprojeto.antifraude.event.consumer;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.service.FraudAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final FraudAnalysisService fraudAnalysisService;

    @KafkaListener(
            topics = "transactions.analysis",
            groupId = "antifraude-group"
    )
    public void consume(TransactionEvent event) {
        log.info("Mensagem recebida do Kafka. Transação ID: {}", event.transactionId());
        fraudAnalysisService.analyze(event);
    }
}