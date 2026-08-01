package rs.pgdavidov.erp.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.pgdavidov.erp.transaction.entity.Transaction;

import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    boolean existsByBankStatementRowId(UUID bankStatementRowId);

    boolean existsByBankStatementRowIdAndIdNot(
            UUID bankStatementRowId,
            UUID id
    );

    @Query(
            value = "SELECT nextval('transaction_code_seq')",
            nativeQuery = true
    )
    long getNextCodeSequenceValue();
}