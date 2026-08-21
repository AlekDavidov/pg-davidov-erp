package rs.pgdavidov.erp.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.common.entity.BaseEntity;
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction extends BaseEntity {

    @Column(
            name = "transaction_code",
            nullable = false,
            unique = true,
            length = 40
    )
    private String transactionCode;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(
            name = "debit",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal debit;

    @Column(
            name = "credit",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal credit;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "raw_counterparty")
    private String rawCounterparty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "bank_statement_row_id", unique = true)
    private UUID bankStatementRowId;

    @Column(name = "reference")
    private String reference;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "transaction_status"
    )
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "source",
            nullable = false,
            columnDefinition = "transaction_source"
    )
    private TransactionSource source;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}