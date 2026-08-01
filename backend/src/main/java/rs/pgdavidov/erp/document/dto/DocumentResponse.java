package rs.pgdavidov.erp.document.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(

        UUID id,

        String documentCode,

        String displayName,

        String filename,

        String contentType,

        Long sizeBytes,

        String storagePath,

        String checksumSha256,

        OffsetDateTime createdAt
) {
}