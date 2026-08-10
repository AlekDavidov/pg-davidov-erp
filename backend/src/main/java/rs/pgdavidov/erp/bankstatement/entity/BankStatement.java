package rs.pgdavidov.erp.bankstatement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_statements")
@Getter
@Setter
@NoArgsConstructor
public class BankStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bank_account_id",
            nullable = false
    )
    private BankAccount bankAccount;

    @Column(
            name = "statement_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String statementCode;

    @Column(name = "period_from")
    private LocalDate periodFrom;

    @Column(name = "period_to")
    private LocalDate periodTo;

    @Column(
            name = "opening_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal openingBalance;

    @Column(
            name = "total_income",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalIncome =
            BigDecimal.ZERO;

    @Column(
            name = "total_expenses",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalExpenses =
            BigDecimal.ZERO;

    @Column(
            name = "closing_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal closingBalance;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "validation_status",
            nullable = false,
            columnDefinition =
                    "statement_validation_status"
    )
    private StatementValidationStatus validationStatus =
            StatementValidationStatus.PENDING;

    @Column(
            name = "original_filename",
            nullable = false,
            length = 255
    )
    private String originalFilename;

    @Column(
            name = "file_checksum_sha256",
            length = 64
    )
    private String fileChecksumSha256;

    @CreationTimestamp
    @Column(
            name = "imported_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime importedAt;
}