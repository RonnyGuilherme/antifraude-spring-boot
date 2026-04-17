package br.com.seuprojeto.antifraude.service;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.dto.TransactionRequest;
import br.com.seuprojeto.antifraude.dto.TransactionResponse;
import br.com.seuprojeto.antifraude.event.producer.TransactionProducer;
import br.com.seuprojeto.antifraude.exception.TransactionNotFoundException;
import br.com.seuprojeto.antifraude.model.Transaction;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;

    public TransactionResponse receiveTransaction(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUserId(request.userId());
        transaction.setAmount(request.amount());
        transaction.setLocation(request.location());
        transaction.setIpAddress(request.ipAddress());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING_ANALYSIS);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transação salva. ID: {}, enviando ao Kafka...", saved.getId());

        TransactionEvent event = new TransactionEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getAmount(),
                saved.getLocation(),
                saved.getIpAddress()
        );
        transactionProducer.sendTransaction(event);

        return toResponse(saved);
    }

    public TransactionResponse findById(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public Page<TransactionResponse> findAll(String userId, TransactionStatus status, Pageable pageable) {
        return transactionRepository
                .findByFilters(userId, status, pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getUserId(),
                t.getAmount(),
                t.getLocation(),
                t.getStatus(),
                t.getTimestamp()
        );
    }
}