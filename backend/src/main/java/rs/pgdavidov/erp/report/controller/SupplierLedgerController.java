package rs.pgdavidov.erp.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.pgdavidov.erp.report.dto.SupplierLedgerResponse;
import rs.pgdavidov.erp.report.service.SupplierLedgerService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/suppliers")
@RequiredArgsConstructor
public class SupplierLedgerController {

    private final SupplierLedgerService
            supplierLedgerService;

    @GetMapping("/{supplierId}/ledger")
    public ResponseEntity<SupplierLedgerResponse> getLedger(
            @PathVariable UUID supplierId,
            @RequestParam LocalDate periodFrom,
            @RequestParam LocalDate periodTo
    ) {
        SupplierLedgerResponse response =
                supplierLedgerService.getLedger(
                        supplierId,
                        periodFrom,
                        periodTo
                );

        return ResponseEntity.ok(response);
    }
}