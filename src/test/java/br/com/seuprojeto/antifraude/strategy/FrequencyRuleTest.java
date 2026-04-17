package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrequencyRuleTest {

    @Mock
    private TransactionRepository transactionRepository;  // Mockito cria um "fake" do repositório

    @InjectMocks
    private FrequencyRule rule;  // Mockito injeta o fake no construtor

    private TransactionEvent event;

    @BeforeEach
    void setUp() {
        event = new TransactionEvent(1L, "user-123", new BigDecimal("300.00"), "SP, BR", "1.2.3.4");
    }

    @Test
    @DisplayName("Deve aprovar quando usuário tem poucas transações recentes")
    void shouldApproveWhenFewRecentTransactions() {
        // Simula o banco retornando 3 transações (abaixo do limite de 5)
        when(transactionRepository.countRecentTransactions(eq("user-123"), any(LocalDateTime.class)))
                .thenReturn(3L);

        RuleResult result = rule.evaluate(event);

        assertThat(result.fraudDetected()).isFalse();
    }

    @Test
    @DisplayName("Deve negar quando usuário excede o limite de transações")
    void shouldDenyWhenTooManyRecentTransactions() {
        // Simula o banco retornando 6 transações (acima do limite de 5)
        when(transactionRepository.countRecentTransactions(eq("user-123"), any(LocalDateTime.class)))
                .thenReturn(6L);

        RuleResult result = rule.evaluate(event);

        assertThat(result.fraudDetected()).isTrue();
        assertThat(result.reason()).contains("user-123");
    }
}