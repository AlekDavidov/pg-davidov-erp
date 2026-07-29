package rs.pgdavidov.erp.invoice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {

    private UUID id;

    private UUID transactionId;

    private BigDecimal amount;

    private OffsetDateTime createdAt;

}