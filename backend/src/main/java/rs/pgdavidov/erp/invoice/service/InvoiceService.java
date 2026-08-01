package rs.pgdavidov.erp.invoice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.common.exception.DuplicateResourceException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.common.pagination.PagedResponse;
import rs.pgdavidov.erp.document.dto.DocumentResponse;
import rs.pgdavidov.erp.document.entity.Document;
import rs.pgdavidov.erp.document.repository.DocumentRepository;
import rs.pgdavidov.erp.document.service.DocumentService;
import rs.pgdavidov.erp.invoice.dto.InvoiceRequest;
import rs.pgdavidov.erp.invoice.dto.InvoiceResponse;
import rs.pgdavidov.erp.invoice.entity.Invoice;
import rs.pgdavidov.erp.invoice.entity.InvoiceDocument;
import rs.pgdavidov.erp.invoice.mapper.InvoiceMapper;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;
import rs.pgdavidov.erp.invoice.repository.InvoiceDocumentRepository;
import rs.pgdavidov.erp.invoice.repository.InvoicePaymentRepository;
import rs.pgdavidov.erp.invoice.repository.InvoiceRepository;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private static final String CODE_PREFIX = "INV";

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDocumentRepository invoiceDocumentRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final SupplierRepository supplierRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceCalculationService invoiceCalculationService;

    public PagedResponse<InvoiceResponse> findAll(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(
                page,
                size,
                sort
        );

        Page<Invoice> invoicePage =
                invoiceRepository.findAll(pageable);

        return PagedResponse.from(
                invoicePage,
                this::toResponse
        );
    }

    public InvoiceResponse findById(UUID id) {
        Invoice invoice = findInvoiceById(id);

        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        validateDueDate(request);

        validateSupplierInvoiceNumberForCreate(
                request.getSupplierId(),
                request.getInvoiceNumber()
        );

        Supplier supplier =
                findSupplierById(
                        request.getSupplierId()
                );

        String invoiceCode =
                generateInvoiceCode();

        Invoice invoice =
                invoiceMapper.toEntity(
                        request,
                        supplier,
                        invoiceCode
                );

        Invoice savedInvoice =
                invoiceRepository.saveAndFlush(
                        invoice
                );

        return toResponse(savedInvoice);
    }

    @Transactional
    public InvoiceResponse update(
            UUID id,
            InvoiceRequest request
    ) {
        Invoice invoice =
                findInvoiceById(id);

        validateDueDate(request);

        validateSupplierInvoiceNumberForUpdate(
                invoice,
                request.getSupplierId(),
                request.getInvoiceNumber()
        );

        Supplier supplier =
                findSupplierById(
                        request.getSupplierId()
                );

        invoiceMapper.updateEntity(
                invoice,
                request,
                supplier
        );

        Invoice savedInvoice =
                invoiceRepository.saveAndFlush(
                        invoice
                );

        return toResponse(savedInvoice);
    }

    @Transactional
    public DocumentResponse uploadDocument(
            UUID invoiceId,
            String displayName,
            MultipartFile file
    ) {
        Invoice invoice =
                findInvoiceById(invoiceId);

        Document document =
                documentService.createDocument(
                        displayName,
                        file
                );

        try {
            InvoiceDocument invoiceDocument =
                    new InvoiceDocument(
                            invoice,
                            document
                    );

            invoiceDocumentRepository.saveAndFlush(
                    invoiceDocument
            );

            return documentService.toResponse(
                    document
            );
        } catch (RuntimeException exception) {
            documentService.deleteDocument(document);

            throw exception;
        }
    }

    public List<DocumentResponse> getDocuments(
            UUID invoiceId
    ) {
        findInvoiceById(invoiceId);

        return invoiceDocumentRepository
                .findAllByInvoice_IdOrderByDocument_CreatedAtDesc(
                        invoiceId
                )
                .stream()
                .map(InvoiceDocument::getDocument)
                .map(documentService::toResponse)
                .toList();
    }

    @Transactional
    public void removeDocument(
            UUID invoiceId,
            UUID documentId
    ) {
        findInvoiceById(invoiceId);

        InvoiceDocument invoiceDocument =
                invoiceDocumentRepository
                        .findByInvoice_IdAndDocument_Id(
                                invoiceId,
                                documentId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document with ID '"
                                                + documentId
                                                + "' is not attached to invoice with ID '"
                                                + invoiceId
                                                + "'."
                                )
                        );

        Document document =
                invoiceDocument.getDocument();

        invoiceDocumentRepository.delete(
                invoiceDocument
        );

        invoiceDocumentRepository.flush();

        boolean attachedToAnotherInvoice =
                invoiceDocumentRepository
                        .countByDocument_Id(
                                documentId
                        ) > 0;

        boolean attachedToTransaction =
                documentRepository
                        .existsTransactionDocumentByDocumentId(
                                documentId
                        );

        if (!attachedToAnotherInvoice
                && !attachedToTransaction) {
            documentService.deleteDocument(
                    document
            );
        }
    }

    @Transactional
    public void delete(UUID id) {
        Invoice invoice =
                findInvoiceById(id);

        List<Document> attachedDocuments =
                invoiceDocumentRepository
                        .findAllByInvoice_IdOrderByDocument_CreatedAtDesc(
                                id
                        )
                        .stream()
                        .map(InvoiceDocument::getDocument)
                        .toList();

        invoiceRepository.delete(invoice);
        invoiceRepository.flush();

        for (Document document : attachedDocuments) {
            UUID documentId =
                    document.getId();

            boolean attachedToAnotherInvoice =
                    invoiceDocumentRepository
                            .countByDocument_Id(
                                    documentId
                            ) > 0;

            boolean attachedToTransaction =
                    documentRepository
                            .existsTransactionDocumentByDocumentId(
                                    documentId
                            );

            if (!attachedToAnotherInvoice
                    && !attachedToTransaction) {
                documentService.deleteDocument(
                        document
                );
            }
        }
    }

    private InvoiceResponse toResponse(
            Invoice invoice
    ) {
        BigDecimal paidAmount =
                invoicePaymentRepository
                        .sumAmountByInvoiceId(
                                invoice.getId()
                        );

        BigDecimal remainingAmount =
                invoiceCalculationService
                        .calculateRemainingAmount(
                                invoice.getAmount(),
                                paidAmount
                        );

        InvoiceStatus status =
                invoiceCalculationService
                        .calculateStatus(
                                invoice.getAmount(),
                                paidAmount
                        );

        return invoiceMapper.toResponse(
                invoice,
                paidAmount,
                remainingAmount,
                status
        );
    }

    private Invoice findInvoiceById(UUID id) {
        return invoiceRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice with ID '"
                                        + id
                                        + "' was not found."
                        )
                );
    }

    private Supplier findSupplierById(
            UUID supplierId
    ) {
        return supplierRepository
                .findById(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier with ID '"
                                        + supplierId
                                        + "' was not found."
                        )
                );
    }

    private String generateInvoiceCode() {
        long sequenceValue =
                invoiceRepository
                        .getNextCodeSequenceValue();

        return CODE_PREFIX + String.format(
                "%06d",
                sequenceValue
        );
    }

    private void validateSupplierInvoiceNumberForCreate(
            UUID supplierId,
            String invoiceNumber
    ) {
        boolean alreadyExists =
                invoiceRepository
                        .existsBySupplierIdAndInvoiceNumber(
                                supplierId,
                                invoiceNumber
                        );

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "Invoice number '"
                            + invoiceNumber
                            + "' already exists for the selected supplier."
            );
        }
    }

    private void validateSupplierInvoiceNumberForUpdate(
            Invoice existingInvoice,
            UUID requestedSupplierId,
            String requestedInvoiceNumber
    ) {
        UUID existingSupplierId =
                existingInvoice
                        .getSupplier()
                        .getId();

        boolean supplierChanged =
                !existingSupplierId.equals(
                        requestedSupplierId
                );

        boolean invoiceNumberChanged =
                !existingInvoice
                        .getInvoiceNumber()
                        .equals(
                                requestedInvoiceNumber
                        );

        if (!supplierChanged
                && !invoiceNumberChanged) {
            return;
        }

        boolean alreadyExists =
                invoiceRepository
                        .existsBySupplierIdAndInvoiceNumber(
                                requestedSupplierId,
                                requestedInvoiceNumber
                        );

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "Invoice number '"
                            + requestedInvoiceNumber
                            + "' already exists for the selected supplier."
            );
        }
    }

    private void validateDueDate(
            InvoiceRequest request
    ) {
        if (request.getDueDate() == null) {
            return;
        }

        if (request
                .getDueDate()
                .isBefore(
                        request.getInvoiceDate()
                )) {
            throw new IllegalArgumentException(
                    "Due date cannot be before invoice date."
            );
        }
    }

    private Sort createSort(
            String sortBy,
            String sortDirection
    ) {
        String resolvedSortBy =
                resolveSortBy(sortBy);

        Sort.Direction direction =
                "desc".equalsIgnoreCase(
                        sortDirection
                )
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return Sort.by(
                direction,
                resolvedSortBy
        );
    }

    private String resolveSortBy(
            String sortBy
    ) {
        if (sortBy == null
                || sortBy.isBlank()) {
            return "invoiceDate";
        }

        return switch (
                sortBy.toLowerCase(Locale.ROOT)
                ) {
            case "id" -> "id";
            case "invoicecode",
                 "invoice_code" -> "invoiceCode";
            case "invoicenumber",
                 "invoice_number" -> "invoiceNumber";
            case "invoicedate",
                 "invoice_date" -> "invoiceDate";
            case "duedate",
                 "due_date" -> "dueDate";
            case "amount" -> "amount";
            case "currencycode",
                 "currency_code" -> "currencyCode";
            case "createdat",
                 "created_at" -> "createdAt";
            case "updatedat",
                 "updated_at" -> "updatedAt";
            default -> "invoiceDate";
        };
    }
}