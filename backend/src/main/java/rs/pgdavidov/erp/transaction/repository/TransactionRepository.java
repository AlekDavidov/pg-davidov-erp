package rs.pgdavidov.erp.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.pgdavidov.erp.transaction.entity.Transaction;

import java.time.LocalDate;
import java.util.List;
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

    @Query("""
            SELECT tx
            FROM Transaction tx
            WHERE tx.supplier.id = :supplierId
              AND tx.transactionDate < :date
              AND tx.debit > 0
              AND NOT EXISTS (
                  SELECT payment.id
                  FROM InvoicePayment payment
                  WHERE payment.transaction.id = tx.id
              )
            ORDER BY tx.transactionDate ASC,
                     tx.id ASC
            """)
    List<Transaction>
    findUnallocatedSupplierDebitTransactionsBefore(
            @Param("supplierId") UUID supplierId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT tx
            FROM Transaction tx
            WHERE tx.supplier.id = :supplierId
              AND tx.transactionDate
                  BETWEEN :periodFrom AND :periodTo
              AND tx.debit > 0
              AND NOT EXISTS (
                  SELECT payment.id
                  FROM InvoicePayment payment
                  WHERE payment.transaction.id = tx.id
              )
            ORDER BY tx.transactionDate ASC,
                     tx.id ASC
            """)
    List<Transaction>
    findUnallocatedSupplierDebitTransactionsBetween(
            @Param("supplierId") UUID supplierId,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo
    );
}