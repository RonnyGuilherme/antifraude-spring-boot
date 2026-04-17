package br.com.seuprojeto.antifraude.event.producer;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProducer {

    private static final String TOPIC = "transactions.analysis";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void sendTransaction(TransactionEvent event) {
        kafkaTemplate.send(TOPIC, event.transactionId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento enviado ao Kafka. ID: {}, tópico: {}, partição: {}",
                                event.transactionId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Falha ao enviar evento ao Kafka. ID: {}, erro: {}",
                                event.transactionId(), ex.getMessage());
                    }
                });
    }
}