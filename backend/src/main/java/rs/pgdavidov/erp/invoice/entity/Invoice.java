package rs.pgdavidov.erp.invoice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.pgdavidov.erp.common.entity.BaseEntity;
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends BaseEntity {

    @Column(name = "invoice_code", nullable = false, unique = true, length = 40)
    private String invoiceCode;

    @Column(name = "invoice_number", nullable = false, length = 150)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(
            name = "currency_code",
            nullable = false,
            columnDefinition = "char(3)"
    )
    private String currencyCode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}