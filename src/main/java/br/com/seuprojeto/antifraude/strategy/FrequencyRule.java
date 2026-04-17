package br.com.seuprojeto.antifraude.strategy;

import br.com.seuprojeto.antifraude.dto.TransactionEvent;
import br.com.seuprojeto.antifraude.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FrequencyRule implements FraudRule {

    private static final int MAX_TRANSACTIONS = 5;
    private static final int WINDOW_MINUTES = 10;

    private final TransactionRepository transactionRepository;

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);

        long count = transactionRepository.countRecentTransactions(
                event.userId(), since
        );

        if (count > MAX_TRANSACTIONS) {
            return RuleResult.denied(
                    "Usuário " + event.userId() + " realizou " + count +
                            " transações nos últimos " + WINDOW_MINUTES + " minutos"
            );
        }
        return RuleResult.approved();
    }
}