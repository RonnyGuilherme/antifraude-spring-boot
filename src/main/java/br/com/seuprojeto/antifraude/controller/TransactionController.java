package br.com.seuprojeto.antifraude.controller;

import br.com.seuprojeto.antifraude.dto.TransactionRequest;
import br.com.seuprojeto.antifraude.dto.TransactionResponse;
import br.com.seuprojeto.antifraude.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Recebimento e análise de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Submeter transação para análise",
            description = "Aceita a transação e a envia para análise assíncrona de fraude.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionRequest.class),
                            examples = @ExampleObject(value = """
                    {
                      "userId": "user-123",
                      "amount": 1500.00,
                      "location": "São Paulo, BR",
                      "ipAddress": "177.42.0.1"
                    }
                """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Transação aceita para análise"),
                    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
            }
    )
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        return transactionService.receiveTransaction(request);
    }
}