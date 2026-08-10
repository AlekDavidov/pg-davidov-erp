package rs.pgdavidov.erp.bankimport.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.dto.BankImportPreviewResponse;
import rs.pgdavidov.erp.bankimport.dto.BankImportRequest;
import rs.pgdavidov.erp.bankimport.dto.BankImportResultResponse;
import rs.pgdavidov.erp.bankimport.dto.SupplierOptionResponse;
import rs.pgdavidov.erp.bankimport.service.BankImportService;

import java.util.List;

@RestController
@RequestMapping("/api/bank-import")
@RequiredArgsConstructor
public class BankImportController {

    private final BankImportService bankImportService;

    @PostMapping(
            value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BankImportPreviewResponse> preview(
            @RequestPart("file") MultipartFile file
    ) {
        BankImportPreviewResponse response =
                bankImportService.preview(file);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BankImportResultResponse> importTransactions(
            @Valid @RequestBody BankImportRequest request
    ) {
        BankImportResultResponse response =
                bankImportService.importTransactions(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierOptionResponse>>
    findSupplierOptions() {
        List<SupplierOptionResponse> response =
                bankImportService.findSupplierOptions();

        return ResponseEntity.ok(response);
    }
}