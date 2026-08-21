package rs.pgdavidov.erp.category.dto;

import rs.pgdavidov.erp.category.entity.CategoryType;

import java.util.UUID;

public record CategoryOptionResponse(

        UUID id,

        String code,

        String name,

        CategoryType categoryType,

        boolean includeInFinancialReport

) {
}