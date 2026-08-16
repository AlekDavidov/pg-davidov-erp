package rs.pgdavidov.erp.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyProfileRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 50)
        String pib,

        @Size(max = 50)
        String registrationNumber,

        @Size(max = 255)
        String address,

        @Size(max = 120)
        String city,

        @Size(max = 20)
        String postalCode,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String bankName,

        @Size(max = 100)
        String bankAccountNumber

) {
}