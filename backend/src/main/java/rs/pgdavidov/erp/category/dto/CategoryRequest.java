package rs.pgdavidov.erp.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rs.pgdavidov.erp.category.entity.CategoryType;

public record CategoryRequest(

        @NotBlank
        @Size(max = 30)
        String code,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        CategoryType categoryType,

        boolean active
) {
}