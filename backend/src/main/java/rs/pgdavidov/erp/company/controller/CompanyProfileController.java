package rs.pgdavidov.erp.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}