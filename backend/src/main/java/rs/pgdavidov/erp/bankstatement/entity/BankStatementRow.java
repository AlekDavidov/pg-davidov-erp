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
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_statement_rows")
@Getter
@Setter
@NoArgsConstructor
public class BankStatementRow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bank_statement_id",
            nullable = false
    )
    private BankStatement bankStatement;

    @Column(
            name = "entry_no",
            nullable = false
    )
    private Integer entryNumber;

    @Column(
            name = "booking_date",
            nullable = false
    )
    private LocalDate bookingDate;

    @Column(name = "execution_date")
    private LocalDate executionDate;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal income =
            BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal expenses =
            BigDecimal.ZERO;

    @Column(
            name = "counterparty_raw",
            length = 500
    )
    private String counterpartyRaw;

    @Column(
            name = "description_raw",
            columnDefinition = "TEXT"
    )
    private String descriptionRaw;

    @Column(
            name = "order_type",
            length = 150
    )
    private String orderType;

    @Column(
            name = "order_reference",
            length = 255
    )
    private String orderReference;

    @Column(
            name = "complaint_reference",
            length = 255
    )
    private String complaintReference;

    @Column(name = "page_number")
    private Integer pageNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_supplier_id")
    private Supplier suggestedSupplier;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "match_status",
            nullable = false,
            columnDefinition = "supplier_match_status"
    )
    private SupplierMatchStatus matchStatus =
            SupplierMatchStatus.UNMATCHED;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;
}