package rs.pgdavidov.erp.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.company.dto.CompanyLogoResponse;
import rs.pgdavidov.erp.company.dto.CompanyProfileRequest;
import rs.pgdavidov.erp.company.dto.CompanyProfileResponse;
import rs.pgdavidov.erp.company.service.CompanyProfileService;

@RestController
@RequestMapping("/api/company-profile")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService
            companyProfileService;

    @GetMapping
    public ResponseEntity<CompanyProfileResponse> getProfile() {
        return companyProfileService
                .getProfile()
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.noContent().build()
                );
    }

    @PutMapping
    public ResponseEntity<CompanyProfileResponse> saveProfile(
            @Valid
            @RequestBody CompanyProfileRequest request
    ) {
        CompanyProfileResponse response =
                companyProfileService.save(
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        companyProfileService.uploadLogo(
                file
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> getLogo() {
        return companyProfileService
                .getLogo()
                .map(this::buildLogoResponse)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @DeleteMapping("/logo")
    public ResponseEntity<Void> deleteLogo() {
        companyProfileService.deleteLogo();

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<byte[]> buildLogoResponse(
            CompanyLogoResponse logo
    ) {
        MediaType mediaType =
                resolveLogoMediaType(
                        logo.filename()
                );

        ContentDisposition contentDisposition =
                ContentDisposition
                        .inline()
                        .filename(
                                logo.filename()
                        )
                        .build();

        return ResponseEntity
                .ok()
                .contentType(
                        mediaType
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .contentLength(
                        logo.content().length
                )
                .body(
                        logo.content()
                );
    }

    private MediaType resolveLogoMediaType(
            String filename
    ) {
        if (
                filename != null
                        && filename
                        .toLowerCase()
                        .endsWith(".png")
        ) {
            return MediaType.IMAGE_PNG;
        }

        return MediaType.IMAGE_JPEG;
    }
}