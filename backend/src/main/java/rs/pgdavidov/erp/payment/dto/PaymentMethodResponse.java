package rs.pgdavidov.erp.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentMethodResponse(
        UUID id,
        String code,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}