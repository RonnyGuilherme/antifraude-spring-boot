package br.com.seuprojeto.antifraude.repository;

import br.com.seuprojeto.antifraude.model.Transaction;
import br.com.seuprojeto.antifraude.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Filtro combinado: userId e status são opcionais
    @Query("""
        SELECT t FROM Transaction t
        WHERE (:userId IS NULL OR t.userId = :userId)
        AND (:status IS NULL OR t.status = :status)
        ORDER BY t.timestamp DESC
    """)
    Page<Transaction> findByFilters(
            @Param("userId") String userId,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );
}