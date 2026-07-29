package rs.pgdavidov.erp.invoice.dto;

import lombok.Getter;
import lombok.Setter;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class InvoiceResponse {

    private UUID id;

    private String invoiceCode;

    private String invoiceNumber;

    private UUID supplierId;

    private String supplierName;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private BigDecimal amount;

    private BigDecimal paidAmount;

    private BigDecimal remainingAmount;

    private InvoiceStatus status;

    private String currencyCode;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}