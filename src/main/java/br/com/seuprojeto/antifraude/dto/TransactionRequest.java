package br.com.seuprojeto.antifraude.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(

        @NotBlank(message = "userId é obrigatório")
        String userId,

        @NotNull(message = "amount é obrigatório")
        @Positive(message = "amount deve ser maior que zero")
        BigDecimal amount,

        String location,

        String ipAddress
) {}
