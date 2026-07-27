package rs.pgdavidov.erp.payment.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.payment.dto.PaymentMethodRequest;
import rs.pgdavidov.erp.payment.dto.PaymentMethodResponse;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;

@Component
public class PaymentMethodMapper {

    public PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getCode(),
                paymentMethod.getName(),
                paymentMethod.isActive(),
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt()
        );
    }

    public PaymentMethod toEntity(PaymentMethodRequest request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setCode(request.code());
        paymentMethod.setName(request.name());
        paymentMethod.setActive(request.active());
        return paymentMethod;
    }

    public void updateEntity(
            PaymentMethod paymentMethod,
            PaymentMethodRequest request
    ) {
        paymentMethod.setCode(request.code());
        paymentMethod.setName(request.name());
        paymentMethod.setActive(request.active());
    }
}