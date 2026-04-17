package br.com.seuprojeto.antifraude.controller;

import br.com.seuprojeto.antifraude.dto.TransactionRequest;
import br.com.seuprojeto.antifraude.dto.TransactionResponse;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import br.com.seuprojeto.antifraude.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Recebimento, consulta e histórico de transações")
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

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar status de uma transação",
            description = "Retorna o status atual da transação. Use após o POST para verificar se foi APPROVED ou DENIED.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transação encontrada"),
                    @ApiResponse(responseCode = "404", description = "Transação não encontrada")
            }
    )
    public TransactionResponse findById(
            @Parameter(description = "ID da transação retornado no POST")
            @PathVariable Long id) {
        return transactionService.findById(id);
    }

    @GetMapping
    @Operation(
            summary = "Listar histórico de transações",
            description = "Retorna lista paginada. Filtre por userId e/ou status. Ex: /transactions?userId=user-123&status=DENIED&page=0&size=10"
    )
    public Page<TransactionResponse> findAll(
            @Parameter(description = "Filtrar por ID do usuário")
            @RequestParam(required = false) String userId,

            @Parameter(description = "Filtrar por status: PENDING_ANALYSIS, APPROVED ou DENIED")
            @RequestParam(required = false) TransactionStatus status,

            @PageableDefault(size = 10, sort = "timestamp") Pageable pageable) {
        return transactionService.findAll(userId, status, pageable);
    }
}