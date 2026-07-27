package rs.pgdavidov.erp.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
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
import rs.pgdavidov.erp.common.exception.ApiErrorResponse;
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

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(
            summary = "Upload document",
            description = "Uploads a PDF, JPEG or PNG document and stores its metadata."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Document uploaded successfully",
                    content = @Content(
                            schema = @Schema(implementation = DocumentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid document code or file",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Document code already exists",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Document storage operation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @Parameter(
                    description = "Unique document code",
                    example = "DOC-2026-0001",
                    required = true
            )
            @RequestParam
            @NotBlank(message = "Document code is required.")
            String documentCode,

            @Parameter(
                    description = "PDF, JPEG or PNG file",
                    required = true
            )
            @RequestPart("file")
            MultipartFile file
    ) {
        DocumentResponse response = documentService.upload(
                documentCode,
                file
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all documents",
            description = "Returns metadata for all stored documents."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Documents returned successfully"
    )
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll() {
        return ResponseEntity.ok(documentService.getAll());
    }

    @Operation(
            summary = "Get document by ID",
            description = "Returns document metadata using its UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document returned successfully",
                    content = @Content(
                            schema = @Schema(implementation = DocumentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(
            @Parameter(
                    description = "Document UUID",
                    required = true
            )
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @Operation(
            summary = "Get document by code",
            description = "Returns document metadata using its unique document code."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document returned successfully",
                    content = @Content(
                            schema = @Schema(implementation = DocumentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping("/code/{documentCode}")
    public ResponseEntity<DocumentResponse> getByDocumentCode(
            @Parameter(
                    description = "Unique document code",
                    example = "DOC-2026-0001",
                    required = true
            )
            @PathVariable String documentCode
    ) {
        return ResponseEntity.ok(
                documentService.getByDocumentCode(documentCode)
        );
    }

    @Operation(
            summary = "Download document",
            description = "Downloads the physical file associated with the document."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document downloaded successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Stored file could not be loaded",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @Parameter(
                    description = "Document UUID",
                    required = true
            )
            @PathVariable UUID id
    ) {
        DocumentDownload document = documentService.download(id);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(document.contentType());
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename(
                        document.filename(),
                        StandardCharsets.UTF_8
                )
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(document.sizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(document.resource());
    }

    @Operation(
            summary = "Delete document",
            description = "Deletes the document metadata and its physical file."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Document deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Stored file could not be deleted",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "Document UUID",
                    required = true
            )
            @PathVariable UUID id
    ) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}