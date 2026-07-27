package rs.pgdavidov.erp.document.dto;

import org.springframework.core.io.Resource;

public record DocumentDownload(
        String filename,
        String contentType,
        Long sizeBytes,
        Resource resource
) {
}