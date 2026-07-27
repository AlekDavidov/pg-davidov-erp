package rs.pgdavidov.erp.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.invoice.entity.Invoice;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByInvoiceCode(String invoiceCode);

    boolean existsBySupplierIdAndInvoiceNumber(UUID supplierId, String invoiceNumber);
}