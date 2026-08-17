package rs.pgdavidov.erp.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rs.pgdavidov.erp.document.entity.DocumentRecordType;

import java.time.LocalDate;
import java.util.UUID;

public record DocumentRecordRequest(

        @NotBlank(message = "Title is required.")
        @Size(max = 255)
        String title,

        @NotNull(message = "Document type is required.")
        DocumentRecordType documentType,

        @Size(max = 100)
        String documentNumber,

        UUID supplierId,

        LocalDate documentDate,

        LocalDate validFrom,

        LocalDate validUntil,

        String notes
) {
}