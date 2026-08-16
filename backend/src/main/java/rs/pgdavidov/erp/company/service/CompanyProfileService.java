package rs.pgdavidov.erp.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.company.dto.CompanyProfileRequest;
import rs.pgdavidov.erp.company.dto.CompanyProfileResponse;
import rs.pgdavidov.erp.company.entity.CompanyProfile;
import rs.pgdavidov.erp.company.repository.CompanyProfileRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

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
                value == null ||
                        value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}