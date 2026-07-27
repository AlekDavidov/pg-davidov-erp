package rs.pgdavidov.erp.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    boolean existsByCode(String code);

    boolean existsByName(String name);
}