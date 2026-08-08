package rs.pgdavidov.erp.bankimport.dto;

import java.util.UUID;

public record SupplierOptionResponse(

        UUID id,

        String code,

        String name,

        UUID defaultCategoryId,

        String defaultCategoryName

) {
}