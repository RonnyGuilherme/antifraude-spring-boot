package br.com.seuprojeto.antifraude.dto;

import br.com.seuprojeto.antifraude.model.TransactionStatus;

import java.time.LocalDateTime;

public record TransactionStatusUpdate(
        Long transactionId,
        String userId,
        TransactionStatus status,
        String message,
        LocalDateTime processedAt
) {}