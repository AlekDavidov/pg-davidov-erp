package rs.pgdavidov.erp.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.category.entity.CategoryType;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNameAndCategoryType(String name, CategoryType categoryType);

    @Query(value = "SELECT nextval('category_code_seq')", nativeQuery = true)
    long getNextCodeSequenceValue();
}