package rs.pgdavidov.erp.document.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.document.dto.DocumentRecordRequest;
import rs.pgdavidov.erp.document.dto.DocumentRecordResponse;
import rs.pgdavidov.erp.document.entity.Document;
import rs.pgdavidov.erp.document.entity.DocumentRecord;
import rs.pgdavidov.erp.document.mapper.DocumentMapper;
import rs.pgdavidov.erp.document.repository.DocumentRecordRepository;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentRecordService {

    private final DocumentRecordRepository
            documentRecordRepository;

    private final SupplierRepository
            supplierRepository;

    private final DocumentMapper
            documentMapper;

    private final DocumentService
            documentService;

    public List<DocumentRecordResponse> getAll() {
        return documentRecordRepository
                .findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                DocumentRecord::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .map(this::toResponse)
                .toList();
    }

    public DocumentRecordResponse getById(
            UUID id
    ) {
        return toResponse(
                findRecord(id)
        );
    }

    @Transactional
    public DocumentRecordResponse create(
            DocumentRecordRequest request
    ) {
        validateValidityPeriod(
                request.validFrom(),
                request.validUntil()
        );

        DocumentRecord record =
                new DocumentRecord();

        applyRequest(
                record,
                request
        );

        DocumentRecord savedRecord =
                documentRecordRepository
                        .saveAndFlush(
                                record
                        );

        return toResponse(
                savedRecord
        );
    }

    @Transactional
    public DocumentRecordResponse update(
            UUID id,
            DocumentRecordRequest request
    ) {
        validateValidityPeriod(
                request.validFrom(),
                request.validUntil()
        );

        DocumentRecord record =
                findRecord(id);

        applyRequest(
                record,
                request
        );

        DocumentRecord savedRecord =
                documentRecordRepository
                        .saveAndFlush(
                                record
                        );

        return toResponse(
                savedRecord
        );
    }

    @Transactional
    public DocumentRecordResponse addDocument(
            UUID id,
            String displayName,
            MultipartFile file
    ) {
        DocumentRecord record =
                findRecord(id);

        Document document =
                documentService.createDocument(
                        displayName,
                        file
                );

        record.getDocuments()
                .add(document);

        DocumentRecord savedRecord =
                documentRecordRepository
                        .saveAndFlush(
                                record
                        );

        return toResponse(
                savedRecord
        );
    }

    @Transactional
    public DocumentRecordResponse removeDocument(
            UUID recordId,
            UUID documentId
    ) {
        DocumentRecord record =
                findRecord(recordId);

        Document document =
                record.getDocuments()
                        .stream()
                        .filter(currentDocument ->
                                currentDocument
                                        .getId()
                                        .equals(documentId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document "
                                                + documentId
                                                + " is not attached to document record "
                                                + recordId
                                )
                        );

        record.getDocuments()
                .remove(document);

        documentRecordRepository
                .saveAndFlush(
                        record
                );

        documentService.deleteDocument(
                document
        );

        return toResponse(
                record
        );
    }

    @Transactional
    public void delete(
            UUID id
    ) {
        DocumentRecord record =
                findRecord(id);

        List<Document> documents =
                List.copyOf(
                        record.getDocuments()
                );

        record.getDocuments()
                .clear();

        documentRecordRepository
                .saveAndFlush(
                        record
                );

        documentRecordRepository.delete(
                record
        );

        documentRecordRepository.flush();

        for (Document document : documents) {
            documentService.deleteDocument(
                    document
            );
        }
    }

    private DocumentRecord findRecord(
            UUID id
    ) {
        return documentRecordRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document record not found: "
                                        + id
                        )
                );
    }

    private void applyRequest(
            DocumentRecord record,
            DocumentRecordRequest request
    ) {
        record.setTitle(
                normalizeRequired(
                        request.title()
                )
        );

        record.setDocumentType(
                request.documentType()
        );

        record.setDocumentNumber(
                normalizeOptional(
                        request.documentNumber()
                )
        );

        record.setSupplier(
                resolveSupplier(
                        request.supplierId()
                )
        );

        record.setDocumentDate(
                request.documentDate()
        );

        record.setValidFrom(
                request.validFrom()
        );

        record.setValidUntil(
                request.validUntil()
        );

        record.setNotes(
                normalizeOptional(
                        request.notes()
                )
        );
    }

    private Supplier resolveSupplier(
            UUID supplierId
    ) {
        if (supplierId == null) {
            return null;
        }

        return supplierRepository
                .findById(
                        supplierId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: "
                                        + supplierId
                        )
                );
    }

    private void validateValidityPeriod(
            LocalDate validFrom,
            LocalDate validUntil
    ) {
        if (
                validFrom != null
                        && validUntil != null
                        && validUntil.isBefore(
                        validFrom
                )
        ) {
            throw new IllegalArgumentException(
                    "Valid until cannot be before valid from."
            );
        }
    }

    private DocumentRecordResponse toResponse(
            DocumentRecord record
    ) {
        Supplier supplier =
                record.getSupplier();

        return new DocumentRecordResponse(
                record.getId(),
                record.getTitle(),
                record.getDocumentType(),
                record.getDocumentNumber(),
                supplier != null
                        ? supplier.getId()
                        : null,
                supplier != null
                        ? supplier.getCode()
                        : null,
                supplier != null
                        ? supplier.getName()
                        : null,
                record.getDocumentDate(),
                record.getValidFrom(),
                record.getValidUntil(),
                record.getNotes(),
                record.getDocuments()
                        .stream()
                        .map(
                                documentMapper::toResponse
                        )
                        .toList(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private String normalizeRequired(
            String value
    ) {
        return value.trim();
    }

    private String normalizeOptional(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}