package rs.pgdavidov.erp.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.pgdavidov.erp.invoice.entity.InvoicePayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoicePaymentRepository
        extends JpaRepository<InvoicePayment, UUID> {

    List<InvoicePayment>
    findAllByInvoice_IdOrderByCreatedAtDesc(
            UUID invoiceId
    );

    Optional<InvoicePayment>
    findByIdAndInvoice_Id(
            UUID id,
            UUID invoiceId
    );

    boolean existsByInvoice_IdAndTransaction_Id(
            UUID invoiceId,
            UUID transactionId
    );

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM InvoicePayment payment
            WHERE payment.invoice.id = :invoiceId
            """)
    BigDecimal sumAmountByInvoiceId(
            @Param("invoiceId") UUID invoiceId
    );

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM InvoicePayment payment
            WHERE payment.transaction.id = :transactionId
            """)
    BigDecimal sumAmountByTransactionId(
            @Param("transactionId") UUID transactionId
    );

    @Query("""
            SELECT payment
            FROM InvoicePayment payment
            JOIN FETCH payment.invoice invoice
            JOIN FETCH payment.transaction transaction
            WHERE invoice.supplier.id = :supplierId
              AND transaction.transactionDate < :date
            ORDER BY transaction.transactionDate ASC,
                     payment.createdAt ASC
            """)
    List<InvoicePayment>
    findAllBySupplierIdAndTransactionDateBefore(
            @Param("supplierId") UUID supplierId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT payment
            FROM InvoicePayment payment
            JOIN FETCH payment.invoice invoice
            JOIN FETCH payment.transaction transaction
            WHERE invoice.supplier.id = :supplierId
              AND transaction.transactionDate
                  BETWEEN :periodFrom AND :periodTo
            ORDER BY transaction.transactionDate ASC,
                     payment.createdAt ASC
            """)
    List<InvoicePayment>
    findAllBySupplierIdAndTransactionDateBetween(
            @Param("supplierId") UUID supplierId,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo
    );

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM InvoicePayment payment
            WHERE payment.invoice.supplier.id = :supplierId
              AND payment.transaction.transactionDate < :date
            """)
    BigDecimal sumAmountBySupplierIdBeforeDate(
            @Param("supplierId") UUID supplierId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM InvoicePayment payment
            WHERE payment.invoice.supplier.id = :supplierId
              AND payment.transaction.transactionDate
                  BETWEEN :periodFrom AND :periodTo
            """)
    BigDecimal sumAmountBySupplierIdAndTransactionDateBetween(
            @Param("supplierId") UUID supplierId,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo
    );
}