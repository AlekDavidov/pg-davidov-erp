package rs.pgdavidov.erp.company.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompanyProfileResponse(

        UUID id,

        String name,

        String pib,

        String registrationNumber,

        String address,

        String city,

        String postalCode,

        String phone,

        String email,

        String bankName,

        String bankAccountNumber,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}