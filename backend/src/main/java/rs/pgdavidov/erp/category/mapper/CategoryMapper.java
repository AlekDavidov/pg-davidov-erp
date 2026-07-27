package rs.pgdavidov.erp.category.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.category.dto.CategoryRequest;
import rs.pgdavidov.erp.category.dto.CategoryResponse;
import rs.pgdavidov.erp.category.entity.Category;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getCategoryType(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setCode(request.code());
        category.setName(request.name());
        category.setCategoryType(request.categoryType());
        category.setActive(request.active());
        return category;
    }

    public void updateEntity(Category category, CategoryRequest request) {
        category.setCode(request.code());
        category.setName(request.name());
        category.setCategoryType(request.categoryType());
        category.setActive(request.active());
    }
}