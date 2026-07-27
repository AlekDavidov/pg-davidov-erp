package rs.pgdavidov.erp.category.dto;

import rs.pgdavidov.erp.category.entity.CategoryType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String code,
        String name,
        CategoryType categoryType,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}