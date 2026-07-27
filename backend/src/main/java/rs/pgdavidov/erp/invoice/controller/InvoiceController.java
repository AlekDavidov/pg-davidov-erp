package rs.pgdavidov.erp.invoice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.pgdavidov.erp.common.pagination.PagedResponse;
import rs.pgdavidov.erp.invoice.dto.InvoiceRequest;
import rs.pgdavidov.erp.invoice.dto.InvoiceResponse;
import rs.pgdavidov.erp.invoice.service.InvoiceService;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
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
}