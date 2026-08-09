package rs.pgdavidov.erp.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rs.pgdavidov.erp.category.dto.CategoryOptionResponse;
import rs.pgdavidov.erp.category.dto.CategoryRequest;
import rs.pgdavidov.erp.category.dto.CategoryResponse;
import rs.pgdavidov.erp.category.service.CategoryService;
import rs.pgdavidov.erp.common.pagination.PagedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public PagedResponse<CategoryResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return categoryService.findAll(
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/options")
    public List<CategoryOptionResponse> findOptions() {
        return categoryService.findOptions();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(
            @PathVariable UUID id
    ) {
        return categoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.update(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        categoryService.delete(id);
    }
}