package rs.pgdavidov.erp.document.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
import rs.pgdavidov.erp.document.dto.DocumentRecordRequest;
import rs.pgdavidov.erp.document.dto.DocumentRecordResponse;
import rs.pgdavidov.erp.document.service.DocumentRecordService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/document-records")
@RequiredArgsConstructor
@Validated
public class DocumentRecordController {

    private final DocumentRecordService
            documentRecordService;

    @GetMapping
    public ResponseEntity<List<DocumentRecordResponse>>
    getAll() {
        return ResponseEntity.ok(
                documentRecordService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentRecordResponse>
    getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                documentRecordService.getById(
                        id
                )
        );
    }

    @PostMapping
    public ResponseEntity<DocumentRecordResponse>
    create(
            @Valid
            @RequestBody DocumentRecordRequest request
    ) {
        return ResponseEntity.ok(
                documentRecordService.create(
                        request
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentRecordResponse>
    update(
            @PathVariable UUID id,
            @Valid
            @RequestBody DocumentRecordRequest request
    ) {
        return ResponseEntity.ok(
                documentRecordService.update(
                        id,
                        request
                )
        );
    }

    @PostMapping(
            value = "/{id}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentRecordResponse>
    addDocument(
            @PathVariable UUID id,
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
        return ResponseEntity.ok(
                documentRecordService.addDocument(
                        id,
                        displayName,
                        file
                )
        );
    }

    @DeleteMapping(
            "/{recordId}/documents/{documentId}"
    )
    public ResponseEntity<DocumentRecordResponse>
    removeDocument(
            @PathVariable UUID recordId,
            @PathVariable UUID documentId
    ) {
        return ResponseEntity.ok(
                documentRecordService.removeDocument(
                        recordId,
                        documentId
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        documentRecordService.delete(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}