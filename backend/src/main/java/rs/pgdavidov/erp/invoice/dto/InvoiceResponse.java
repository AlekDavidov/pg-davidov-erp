package rs.pgdavidov.erp.invoice.dto;

import lombok.Getter;
import lombok.Setter;

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

    private String currencyCode;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}