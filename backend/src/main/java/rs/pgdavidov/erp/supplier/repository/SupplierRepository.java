package rs.pgdavidov.erp.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    boolean existsByName(String name);

    @Query(
            value = "SELECT nextval('supplier_code_seq')",
            nativeQuery = true
    )
    long getNextCodeSequenceValue();
}