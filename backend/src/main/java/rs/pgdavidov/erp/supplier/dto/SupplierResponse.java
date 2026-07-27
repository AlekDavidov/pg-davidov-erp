package rs.pgdavidov.erp.supplier.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SupplierResponse(

        UUID id,

        String code,

        String name,

        UUID defaultCategoryId,
        String defaultCategoryName,

        UUID paymentMethodId,
        String paymentMethodName,

        Integer paymentTerms,

        String pib,
        String registrationNumber,
        String phone,
        String email,
        String contactPerson,
        String notes,

        boolean active,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}