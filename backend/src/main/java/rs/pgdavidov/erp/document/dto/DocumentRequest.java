package rs.pgdavidov.erp.document.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(

        @NotBlank(message = "Document code is required.")
        String documentCode
) {
}