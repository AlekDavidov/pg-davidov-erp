package rs.pgdavidov.erp.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SupplierRequest(

        @NotBlank
        @Size(max = 30)
        String code,

        @NotBlank
        @Size(max = 200)
        String name,

        @NotNull
        UUID defaultCategoryId,

        @NotNull
        UUID paymentMethodId,

        @NotNull
        Integer paymentTerms,

        @Size(max = 20)
        String pib,

        @Size(max = 20)
        String registrationNumber,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 100)
        String contactPerson,

        String notes,

        boolean active
) {
}