package rs.pgdavidov.erp.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.company.dto.CompanyLogoResponse;
import rs.pgdavidov.erp.company.dto.CompanyProfileRequest;
import rs.pgdavidov.erp.company.dto.CompanyProfileResponse;
import rs.pgdavidov.erp.company.entity.CompanyProfile;
import rs.pgdavidov.erp.company.repository.CompanyProfileRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private static final long MAX_LOGO_SIZE =
            5L * 1024L * 1024L;

    private static final String CONTENT_TYPE_PNG =
            "image/png";

    private static final String CONTENT_TYPE_JPEG =
            "image/jpeg";

    private final CompanyProfileRepository
            companyProfileRepository;

    @Transactional(readOnly = true)
    public Optional<CompanyProfileResponse> getProfile() {
        return companyProfileRepository
                .findAll()
                .stream()
                .findFirst()
                .map(this::toResponse);
    }

    @Transactional
    public CompanyProfileResponse save(
            CompanyProfileRequest request
    ) {
        CompanyProfile profile =
                companyProfileRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            CompanyProfile newProfile =
                                    new CompanyProfile();

                            newProfile.setId(
                                    UUID.randomUUID()
                            );

                            return newProfile;
                        });

        applyRequest(
                profile,
                request
        );

        CompanyProfile savedProfile =
                companyProfileRepository.save(
                        profile
                );

        return toResponse(
                savedProfile
        );
    }

    @Transactional
    public void uploadLogo(
            MultipartFile file
    ) {
        validateLogo(
                file
        );

        CompanyProfile profile =
                getRequiredProfile();

        try {
            profile.setLogoFilename(
                    normalizeFilename(
                            file.getOriginalFilename()
                    )
            );

            profile.setLogoContent(
                    file.getBytes()
            );

            companyProfileRepository.save(
                    profile
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Logo could not be read.",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public Optional<CompanyLogoResponse> getLogo() {
        return companyProfileRepository
                .findAll()
                .stream()
                .findFirst()
                .filter(profile ->
                        profile.getLogoContent() != null
                                && profile.getLogoContent().length > 0
                )
                .map(profile ->
                        new CompanyLogoResponse(
                                profile.getLogoFilename(),
                                profile.getLogoContent()
                        )
                );
    }

    @Transactional
    public void deleteLogo() {
        CompanyProfile profile =
                getRequiredProfile();

        profile.setLogoFilename(
                null
        );

        profile.setLogoContent(
                null
        );

        companyProfileRepository.save(
                profile
        );
    }

    private CompanyProfile getRequiredProfile() {
        return companyProfileRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Company profile must be created before uploading a logo."
                        )
                );
    }

    private void validateLogo(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Logo file is required."
            );
        }

        if (
                file.getSize() >
                        MAX_LOGO_SIZE
        ) {
            throw new IllegalArgumentException(
                    "Logo file must not exceed 5 MB."
            );
        }

        String contentType =
                file.getContentType();

        if (
                !CONTENT_TYPE_PNG.equalsIgnoreCase(
                        contentType
                )
                        && !CONTENT_TYPE_JPEG.equalsIgnoreCase(
                        contentType
                )
        ) {
            throw new IllegalArgumentException(
                    "Only PNG and JPEG logo files are supported."
            );
        }

        String filename =
                file.getOriginalFilename();

        if (
                filename == null
                        || filename.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Logo filename is required."
            );
        }

        String normalizedFilename =
                filename.toLowerCase();

        if (
                !normalizedFilename.endsWith(
                        ".png"
                )
                        && !normalizedFilename.endsWith(
                        ".jpg"
                )
                        && !normalizedFilename.endsWith(
                        ".jpeg"
                )
        ) {
            throw new IllegalArgumentException(
                    "Logo file must have a PNG, JPG or JPEG extension."
            );
        }
    }

    private String normalizeFilename(
            String filename
    ) {
        if (filename == null) {
            return null;
        }

        String normalized =
                filename.replace(
                        "\\",
                        "/"
                );

        int lastSlash =
                normalized.lastIndexOf('/');

        if (lastSlash >= 0) {
            normalized =
                    normalized.substring(
                            lastSlash + 1
                    );
        }

        return normalized.trim();
    }

    private void applyRequest(
            CompanyProfile profile,
            CompanyProfileRequest request
    ) {
        profile.setName(
                normalizeRequired(
                        request.name()
                )
        );

        profile.setPib(
                normalizeOptional(
                        request.pib()
                )
        );

        profile.setRegistrationNumber(
                normalizeOptional(
                        request.registrationNumber()
                )
        );

        profile.setAddress(
                normalizeOptional(
                        request.address()
                )
        );

        profile.setCity(
                normalizeOptional(
                        request.city()
                )
        );

        profile.setPostalCode(
                normalizeOptional(
                        request.postalCode()
                )
        );

        profile.setPhone(
                normalizeOptional(
                        request.phone()
                )
        );

        profile.setEmail(
                normalizeOptional(
                        request.email()
                )
        );

        profile.setBankName(
                normalizeOptional(
                        request.bankName()
                )
        );

        profile.setBankAccountNumber(
                normalizeOptional(
                        request.bankAccountNumber()
                )
        );
    }

    private CompanyProfileResponse toResponse(
            CompanyProfile profile
    ) {
        return new CompanyProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getPib(),
                profile.getRegistrationNumber(),
                profile.getAddress(),
                profile.getCity(),
                profile.getPostalCode(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getBankName(),
                profile.getBankAccountNumber(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private String normalizeRequired(
            String value
    ) {
        return value.trim();
    }

    private String normalizeOptional(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}