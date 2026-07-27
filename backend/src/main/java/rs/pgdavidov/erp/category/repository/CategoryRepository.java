package rs.pgdavidov.erp.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.category.entity.CategoryType;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNameAndCategoryType(String name, CategoryType categoryType);
}