package br.com.seuprojeto.antifraude.dto;

import br.com.seuprojeto.antifraude.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String userId,
        BigDecimal amount,
        String location,
        TransactionStatus status,
        LocalDateTime timestamp
) {}
