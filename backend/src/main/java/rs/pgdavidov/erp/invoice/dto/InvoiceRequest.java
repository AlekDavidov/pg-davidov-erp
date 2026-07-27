package rs.pgdavidov.erp.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class InvoiceRequest {

    @NotBlank
    @Size(max = 40)
    private String invoiceCode;

    @NotBlank
    @Size(max = 150)
    private String invoiceNumber;

    @NotNull
    private UUID supplierId;

    @NotNull
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;

    @Size(max = 2000)
    private String notes;
}