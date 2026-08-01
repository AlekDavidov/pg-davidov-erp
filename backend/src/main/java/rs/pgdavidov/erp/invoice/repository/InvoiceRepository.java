package rs.pgdavidov.erp.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.pgdavidov.erp.invoice.entity.Invoice;

import java.util.UUID;

public interface InvoiceRepository
        extends JpaRepository<Invoice, UUID> {

    boolean existsBySupplierIdAndInvoiceNumber(
            UUID supplierId,
            String invoiceNumber
    );

    @Query(
            value = "SELECT nextval('invoice_code_seq')",
            nativeQuery = true
    )
    long getNextCodeSequenceValue();
}