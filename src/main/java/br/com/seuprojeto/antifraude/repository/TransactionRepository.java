package br.com.seuprojeto.antifraude.repository;

import br.com.seuprojeto.antifraude.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT COUNT(t) FROM Transaction t
        WHERE t.userId = :userId
        AND t.timestamp >= :since
    """)
    long countRecentTransactions(
            @Param("userId") String userId,
            @Param("since") LocalDateTime since
    );
}