package br.com.seuprojeto.antifraude.dto;

import java.math.BigDecimal;

public record TransactionEvent(
        Long transactionId,
        String userId,
        BigDecimal amount,
        String location,
        String ipAddress
) {}
