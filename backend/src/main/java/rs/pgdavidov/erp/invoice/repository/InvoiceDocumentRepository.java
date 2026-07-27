package rs.pgdavidov.erp.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.invoice.entity.InvoiceDocument;
import rs.pgdavidov.erp.invoice.entity.InvoiceDocumentId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceDocumentRepository
        extends JpaRepository<InvoiceDocument, InvoiceDocumentId> {

    List<InvoiceDocument> findAllByInvoice_IdOrderByDocument_CreatedAtDesc(
            UUID invoiceId
    );

    Optional<InvoiceDocument> findByInvoice_IdAndDocument_Id(
            UUID invoiceId,
            UUID documentId
    );

    boolean existsByInvoice_IdAndDocument_Id(
            UUID invoiceId,
            UUID documentId
    );

    long countByDocument_Id(UUID documentId);
}