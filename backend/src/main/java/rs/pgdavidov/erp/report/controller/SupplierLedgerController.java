package rs.pgdavidov.erp.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.pgdavidov.erp.report.dto.SupplierLedgerExport;
import rs.pgdavidov.erp.report.dto.SupplierLedgerResponse;
import rs.pgdavidov.erp.report.service.SupplierLedgerExportService;
import rs.pgdavidov.erp.report.service.SupplierLedgerService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/suppliers")
@RequiredArgsConstructor
public class SupplierLedgerController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

    private final SupplierLedgerService
            supplierLedgerService;

    private final SupplierLedgerExportService
            supplierLedgerExportService;

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

    @GetMapping("/{supplierId}/ledger/export")
    public ResponseEntity<byte[]> exportLedger(
            @PathVariable UUID supplierId,
            @RequestParam LocalDate periodFrom,
            @RequestParam LocalDate periodTo
    ) {
        SupplierLedgerExport export =
                supplierLedgerExportService.export(
                        supplierId,
                        periodFrom,
                        periodTo
                );

        ContentDisposition contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                export.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity
                .ok()
                .contentType(
                        XLSX_MEDIA_TYPE
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .contentLength(
                        export.content().length
                )
                .body(
                        export.content()
                );
    }
}