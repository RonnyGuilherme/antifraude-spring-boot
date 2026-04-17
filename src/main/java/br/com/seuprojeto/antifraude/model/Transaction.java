package br.com.seuprojeto.antifraude.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data                  // Lombok: gera getters, setters, equals, hashCode, toString
@NoArgsConstructor     // Lombok: gera construtor vazio (exigido pelo JPA)
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String location;   // Ex: "São Paulo, BR"

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING) // Salva "PENDING_ANALYSIS" no banco, não o número
    @Column(nullable = false)
    private TransactionStatus status;
}