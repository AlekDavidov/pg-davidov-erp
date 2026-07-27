package rs.pgdavidov.erp.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.transaction.entity.Transaction;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    boolean existsByTransactionCode(String transactionCode);

    boolean existsByBankStatementRowId(UUID bankStatementRowId);

    boolean existsByBankStatementRowIdAndIdNot(
            UUID bankStatementRowId,
            UUID id
    );
}