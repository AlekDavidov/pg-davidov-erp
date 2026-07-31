package rs.pgdavidov.erp.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;

import java.util.UUID;

public interface PaymentMethodRepository
        extends JpaRepository<PaymentMethod, UUID> {

    boolean existsByName(String name);

    @Query(
            value = "SELECT nextval('payment_method_code_seq')",
            nativeQuery = true
    )
    long getNextCodeSequenceValue();
}