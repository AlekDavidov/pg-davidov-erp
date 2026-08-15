package rs.pgdavidov.erp.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.pgdavidov.erp.invoice.entity.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository
        extends JpaRepository<Invoice, UUID> {

    boolean existsByInvoiceCode(
            String invoiceCode
    );

    boolean existsBySupplierIdAndInvoiceNumber(
            UUID supplierId,
            String invoiceNumber
    );

    @Query(
            value = """
                    SELECT nextval(
                        'invoice_code_seq'
                    )
                    """,
            nativeQuery = true
    )
    long getNextCodeSequenceValue();

    List<Invoice>
    findAllBySupplierIdAndInvoiceDateBeforeOrderByInvoiceDateAsc(
            UUID supplierId,
            LocalDate date
    );

    List<Invoice>
    findAllBySupplierIdAndInvoiceDateBetweenOrderByInvoiceDateAsc(
            UUID supplierId,
            LocalDate periodFrom,
            LocalDate periodTo
    );
}