package rs.pgdavidov.erp.supplier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.common.entity.BaseEntity;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
public class Supplier extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_category_id")
    private Category defaultCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_terms", nullable = false)
    private Integer paymentTerms = 0;

    @Column(length = 20)
    private String pib;

    @Column(name = "registration_number", length = 20)
    private String registrationNumber;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;
}