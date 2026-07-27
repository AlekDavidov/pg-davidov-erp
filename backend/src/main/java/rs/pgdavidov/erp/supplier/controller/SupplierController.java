package rs.pgdavidov.erp.supplier.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rs.pgdavidov.erp.common.pagination.PagedResponse;
import rs.pgdavidov.erp.supplier.dto.SupplierRequest;
import rs.pgdavidov.erp.supplier.dto.SupplierResponse;
import rs.pgdavidov.erp.supplier.service.SupplierService;

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public PagedResponse<SupplierResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return supplierService.findAll(
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable UUID id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(
            @Valid @RequestBody SupplierRequest request
    ) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierRequest request
    ) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        supplierService.delete(id);
    }
}