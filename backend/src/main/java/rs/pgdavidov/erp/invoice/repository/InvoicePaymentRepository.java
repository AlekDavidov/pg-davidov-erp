package rs.pgdavidov.erp.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.pgdavidov.erp.invoice.entity.InvoicePayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoicePaymentRepository
        extends JpaRepository<InvoicePayment, UUID> {

    List<InvoicePayment> findAllByInvoice_IdOrderByCreatedAtDesc(
            UUID invoiceId
    );

    Optional<InvoicePayment> findByIdAndInvoice_Id(
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
}