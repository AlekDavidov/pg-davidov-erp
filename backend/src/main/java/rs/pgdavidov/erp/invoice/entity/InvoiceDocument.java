package rs.pgdavidov.erp.invoice.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.pgdavidov.erp.document.entity.Document;

@Entity
@Table(name = "invoice_documents")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceDocument {

    @EmbeddedId
    private InvoiceDocumentId id;

    @MapsId("invoiceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @MapsId("documentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    public InvoiceDocument(
            Invoice invoice,
            Document document
    ) {
        this.invoice = invoice;
        this.document = document;
        this.id = new InvoiceDocumentId(
                invoice.getId(),
                document.getId()
        );
    }
}