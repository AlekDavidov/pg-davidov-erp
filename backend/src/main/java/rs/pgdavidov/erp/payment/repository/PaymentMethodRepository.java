package rs.pgdavidov.erp.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    Optional<PaymentMethod> findByCode(String code);

    boolean existsByCode(String code);
}