package rs.pgdavidov.erp.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentMethodRequest(

        @NotBlank
        @Size(max = 30)
        String code,

        @NotBlank
        @Size(max = 100)
        String name,

        boolean active
) {
}