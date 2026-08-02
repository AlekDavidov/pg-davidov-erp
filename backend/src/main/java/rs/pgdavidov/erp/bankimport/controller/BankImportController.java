package rs.pgdavidov.erp.bankimport.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.dto.BankImportPreviewResponse;
import rs.pgdavidov.erp.bankimport.service.BankImportService;

@RestController
@RequestMapping("/api/bank-import")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Bank Import",
        description = "Bank statement preview and import"
)
public class BankImportController {

    private final BankImportService bankImportService;

    @Operation(
            summary = "Preview bank statement",
            description =
                    "Automatically detects the supported bank statement format "
                            + "and returns parsed transactions without saving them."
    )
    @PostMapping(
            value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BankImportPreviewResponse> preview(
            @Parameter(
                    description = "Bank statement file",
                    required = true
            )
            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                bankImportService.preview(file)
        );
    }
}