package rs.pgdavidov.erp.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.supplier.entity.SupplierAlias;

import java.util.Optional;
import java.util.UUID;

public interface SupplierAliasRepository
        extends JpaRepository<SupplierAlias, UUID> {

    Optional<SupplierAlias>
    findByNormalizedAliasAndActiveTrue(
            String normalizedAlias
    );

    boolean existsByNormalizedAlias(
            String normalizedAlias
    );
}