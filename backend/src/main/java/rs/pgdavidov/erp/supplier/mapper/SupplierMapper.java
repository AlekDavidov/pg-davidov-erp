package rs.pgdavidov.erp.supplier.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;
import rs.pgdavidov.erp.supplier.dto.SupplierRequest;
import rs.pgdavidov.erp.supplier.dto.SupplierResponse;
import rs.pgdavidov.erp.supplier.entity.Supplier;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {

        return new SupplierResponse(
                supplier.getId(),
                supplier.getCode(),
                supplier.getName(),

                supplier.getDefaultCategory() != null
                        ? supplier.getDefaultCategory().getId()
                        : null,

                supplier.getDefaultCategory() != null
                        ? supplier.getDefaultCategory().getName()
                        : null,

                supplier.getPaymentMethod() != null
                        ? supplier.getPaymentMethod().getId()
                        : null,

                supplier.getPaymentMethod() != null
                        ? supplier.getPaymentMethod().getName()
                        : null,

                supplier.getPaymentTerms(),
                supplier.getPib(),
                supplier.getRegistrationNumber(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getContactPerson(),
                supplier.getNotes(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }

    public Supplier toEntity(
            SupplierRequest request,
            Category category,
            PaymentMethod paymentMethod
    ) {

        Supplier supplier = new Supplier();

        updateEntity(
                supplier,
                request,
                category,
                paymentMethod
        );

        return supplier;
    }

    public void updateEntity(
            Supplier supplier,
            SupplierRequest request,
            Category category,
            PaymentMethod paymentMethod
    ) {

        supplier.setCode(request.code());
        supplier.setName(request.name());
        supplier.setDefaultCategory(category);
        supplier.setPaymentMethod(paymentMethod);
        supplier.setPaymentTerms(request.paymentTerms());
        supplier.setPib(request.pib());
        supplier.setRegistrationNumber(request.registrationNumber());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setContactPerson(request.contactPerson());
        supplier.setNotes(request.notes());
        supplier.setActive(request.active());
    }
}