package rs.pgdavidov.erp.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.category.dto.CategoryRequest;
import rs.pgdavidov.erp.category.dto.CategoryResponse;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.category.mapper.CategoryMapper;
import rs.pgdavidov.erp.category.repository.CategoryRepository;
import rs.pgdavidov.erp.common.exception.DuplicateResourceException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.common.pagination.PagedResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public PagedResponse<CategoryResponse> findAll(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Category> categories = categoryRepository.findAll(pageRequest);

        return PagedResponse.from(
                categories,
                categoryMapper::toResponse
        );
    }

    public CategoryResponse findById(UUID id) {
        return categoryMapper.toResponse(getEntity(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {

        if (categoryRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException(
                    "Category code already exists: " + request.code()
            );
        }

        if (categoryRepository.existsByNameAndCategoryType(
                request.name(),
                request.categoryType()
        )) {
            throw new DuplicateResourceException(
                    "Category name already exists for type: "
                            + request.categoryType()
            );
        }

        Category category = categoryMapper.toEntity(request);

        return categoryMapper.toResponse(
                categoryRepository.saveAndFlush(category)
        );
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {

        Category category = getEntity(id);

        if (!category.getCode().equals(request.code())
                && categoryRepository.existsByCode(request.code())) {

            throw new DuplicateResourceException(
                    "Category code already exists: " + request.code()
            );
        }

        boolean changed =
                !category.getName().equals(request.name())
                        || category.getCategoryType() != request.categoryType();

        if (changed
                && categoryRepository.existsByNameAndCategoryType(
                request.name(),
                request.categoryType()
        )) {

            throw new DuplicateResourceException(
                    "Category name already exists for type: "
                            + request.categoryType()
            );
        }

        categoryMapper.updateEntity(category, request);

        return categoryMapper.toResponse(
                categoryRepository.saveAndFlush(category)
        );
    }

    @Transactional
    public void delete(UUID id) {
        categoryRepository.delete(getEntity(id));
    }

    private Category getEntity(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + id
                        ));
    }
}