package rs.pgdavidov.erp.supplier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.category.repository.CategoryRepository;
import rs.pgdavidov.erp.common.exception.DuplicateResourceException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.common.pagination.PagedResponse;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;
import rs.pgdavidov.erp.payment.repository.PaymentMethodRepository;
import rs.pgdavidov.erp.supplier.dto.SupplierRequest;
import rs.pgdavidov.erp.supplier.dto.SupplierResponse;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.mapper.SupplierMapper;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private static final String CODE_PREFIX = "SUP";

    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SupplierMapper supplierMapper;

    public PagedResponse<SupplierResponse> findAll(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Supplier> suppliers =
                supplierRepository.findAll(pageRequest);

        return PagedResponse.from(
                suppliers,
                supplierMapper::toResponse
        );
    }

    public SupplierResponse findById(UUID id) {
        return supplierMapper.toResponse(getSupplier(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        validateUniqueNameForCreate(request.name());

        Category category =
                getCategory(request.defaultCategoryId());

        PaymentMethod paymentMethod =
                getPaymentMethod(request.paymentMethodId());

        String code = generateCode();

        Supplier supplier = supplierMapper.toEntity(
                request,
                code,
                category,
                paymentMethod
        );

        return supplierMapper.toResponse(
                supplierRepository.saveAndFlush(supplier)
        );
    }

    @Transactional
    public SupplierResponse update(
            UUID id,
            SupplierRequest request
    ) {
        Supplier supplier = getSupplier(id);

        validateUniqueNameForUpdate(
                supplier,
                request.name()
        );

        Category category =
                getCategory(request.defaultCategoryId());

        PaymentMethod paymentMethod =
                getPaymentMethod(request.paymentMethodId());

        supplierMapper.updateEntity(
                supplier,
                request,
                category,
                paymentMethod
        );

        return supplierMapper.toResponse(
                supplierRepository.saveAndFlush(supplier)
        );
    }

    @Transactional
    public void delete(UUID id) {
        supplierRepository.delete(getSupplier(id));
    }

    private String generateCode() {
        long sequenceValue =
                supplierRepository.getNextCodeSequenceValue();

        return CODE_PREFIX + String.format(
                "%04d",
                sequenceValue
        );
    }

    private void validateUniqueNameForCreate(String name) {
        if (supplierRepository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Supplier name already exists: " + name
            );
        }
    }

    private void validateUniqueNameForUpdate(
            Supplier supplier,
            String name
    ) {
        if (!supplier.getName().equals(name)
                && supplierRepository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Supplier name already exists: " + name
            );
        }
    }

    private Supplier getSupplier(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + id
                        )
                );
    }

    private PaymentMethod getPaymentMethod(UUID id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment method not found: " + id
                        )
                );
    }
}