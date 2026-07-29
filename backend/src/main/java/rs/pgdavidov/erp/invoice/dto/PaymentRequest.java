package rs.pgdavidov.erp.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {

    @NotNull
    private UUID transactionId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

}