package rs.pgdavidov.erp.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_profile")
@Getter
@Setter
@NoArgsConstructor
public class CompanyProfile {

    @Id
    private UUID id;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            name = "pib",
            length = 50
    )
    private String pib;

    @Column(
            name = "registration_number",
            length = 50
    )
    private String registrationNumber;

    @Column(
            name = "address",
            length = 255
    )
    private String address;

    @Column(
            name = "city",
            length = 120
    )
    private String city;

    @Column(
            name = "postal_code",
            length = 20
    )
    private String postalCode;

    @Column(
            name = "phone",
            length = 50
    )
    private String phone;

    @Column(
            name = "email",
            length = 255
    )
    private String email;

    @Column(
            name = "bank_name",
            length = 255
    )
    private String bankName;

    @Column(
            name = "bank_account_number",
            length = 100
    )
    private String bankAccountNumber;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;
}