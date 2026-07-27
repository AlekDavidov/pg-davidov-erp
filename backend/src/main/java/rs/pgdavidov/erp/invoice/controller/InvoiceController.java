package rs.pgdavidov.erp.invoice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.common.pagination.PagedResponse;
import rs.pgdavidov.erp.document.dto.DocumentResponse;
import rs.pgdavidov.erp.invoice.dto.InvoiceRequest;
import rs.pgdavidov.erp.invoice.dto.InvoiceResponse;
import rs.pgdavidov.erp.invoice.service.InvoiceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Validated
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<PagedResponse<InvoiceResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PagedResponse<InvoiceResponse> response =
                invoiceService.findAll(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> findById(
            @PathVariable UUID id
    ) {
        InvoiceResponse response = invoiceService.findById(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody InvoiceRequest request
    ) {
        InvoiceResponse response = invoiceService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceRequest request
    ) {
        InvoiceResponse response =
                invoiceService.update(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        invoiceService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/{invoiceId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable UUID invoiceId,
            @RequestParam
            @NotBlank(message = "Document code is required.")
            String documentCode,
            @RequestPart("file") MultipartFile file
    ) {
        DocumentResponse response =
                invoiceService.uploadDocument(
                        invoiceId,
                        documentCode,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{invoiceId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable UUID invoiceId
    ) {
        List<DocumentResponse> response =
                invoiceService.getDocuments(invoiceId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{invoiceId}/documents/{documentId}")
    public ResponseEntity<Void> removeDocument(
            @PathVariable UUID invoiceId,
            @PathVariable UUID documentId
    ) {
        invoiceService.removeDocument(
                invoiceId,
                documentId
        );

        return ResponseEntity.noContent().build();
    }
}