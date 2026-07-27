package rs.pgdavidov.erp.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.pgdavidov.erp.common.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}