package rs.pgdavidov.erp.document.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.document.dto.DocumentRequest;
import rs.pgdavidov.erp.document.dto.DocumentResponse;
import rs.pgdavidov.erp.document.entity.Document;

@Component
public class DocumentMapper {

    public Document toEntity(DocumentRequest request) {
        Document document = new Document();
        document.setDocumentCode(request.documentCode());

        return document;
    }

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentCode(),
                document.getFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getStoragePath(),
                document.getChecksumSha256(),
                document.getCreatedAt()
        );
    }
}