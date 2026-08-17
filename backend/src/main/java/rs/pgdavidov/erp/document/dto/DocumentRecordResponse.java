package rs.pgdavidov.erp.document.dto;

import rs.pgdavidov.erp.document.entity.DocumentRecordType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentRecordResponse(

        UUID id,

        String title,

        DocumentRecordType documentType,

        String documentNumber,

        UUID supplierId,

        String supplierCode,

        String supplierName,

        LocalDate documentDate,

        LocalDate validFrom,

        LocalDate validUntil,

        String notes,

        List<DocumentResponse> documents,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}