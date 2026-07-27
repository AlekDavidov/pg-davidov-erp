package rs.pgdavidov.erp.bankaccount.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.pgdavidov.erp.common.entity.BaseEntity;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
public class BankAccount extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "bank_name", nullable = false, length = 150)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "active", nullable = false)
    private Boolean active;
}