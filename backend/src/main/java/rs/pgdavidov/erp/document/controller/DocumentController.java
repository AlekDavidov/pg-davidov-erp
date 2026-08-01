package rs.pgdavidov.erp.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.document.dto.DocumentDownload;
import rs.pgdavidov.erp.document.dto.DocumentResponse;
import rs.pgdavidov.erp.document.service.DocumentService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Validated
@Tag(
        name = "Documents",
        description = "Document upload, retrieval, download and deletion"
)
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService
    ) {
        this.documentService = documentService;
    }

    @Operation(
            summary = "Upload document",
            description = "Uploads a PDF, JPEG or PNG document."
    )
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam
            @NotBlank(
                    message = "Document display name is required."
            )
            @Size(
                    max = 255,
                    message = "Document display name cannot exceed 255 characters."
            )
            String displayName,
            @RequestPart("file")
            MultipartFile file
    ) {
        DocumentResponse response =
                documentService.upload(
                        displayName,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Get all documents")
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll() {
        return ResponseEntity.ok(
                documentService.getAll()
        );
    }

    @Operation(summary = "Get document by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                documentService.getById(id)
        );
    }

    @Operation(summary = "Get document by code")
    @GetMapping("/code/{documentCode}")
    public ResponseEntity<DocumentResponse> getByDocumentCode(
            @PathVariable String documentCode
    ) {
        return ResponseEntity.ok(
                documentService
                        .getByDocumentCode(
                                documentCode
                        )
        );
    }

    @Operation(summary = "Download document")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @Parameter(
                    description = "Document UUID",
                    required = true
            )
            @PathVariable UUID id
    ) {
        DocumentDownload document =
                documentService.download(id);

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            document.contentType()
                    );
        } catch (IllegalArgumentException exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                document.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .contentLength(document.sizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(document.resource());
    }

    @Operation(summary = "Delete document")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        documentService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}