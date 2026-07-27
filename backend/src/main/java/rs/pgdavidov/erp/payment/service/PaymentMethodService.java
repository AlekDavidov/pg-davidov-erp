package rs.pgdavidov.erp.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.payment.dto.PaymentMethodRequest;
import rs.pgdavidov.erp.payment.dto.PaymentMethodResponse;
import rs.pgdavidov.erp.payment.entity.PaymentMethod;
import rs.pgdavidov.erp.payment.mapper.PaymentMethodMapper;
import rs.pgdavidov.erp.payment.repository.PaymentMethodRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    public List<PaymentMethodResponse> findAll() {
        return paymentMethodRepository.findAll()
                .stream()
                .map(paymentMethodMapper::toResponse)
                .toList();
    }

    public PaymentMethodResponse findById(UUID id) {
        return paymentMethodMapper.toResponse(getEntity(id));
    }

    @Transactional
    public PaymentMethodResponse create(PaymentMethodRequest request) {
        if (paymentMethodRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException(
                    "Payment method code already exists: " + request.code()
            );
        }

        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(request);
        PaymentMethod savedPaymentMethod =
                paymentMethodRepository.saveAndFlush(paymentMethod);

        return paymentMethodMapper.toResponse(savedPaymentMethod);
    }

    @Transactional
    public PaymentMethodResponse update(
            UUID id,
            PaymentMethodRequest request
    ) {
        PaymentMethod paymentMethod = getEntity(id);

        if (!paymentMethod.getCode().equals(request.code())
                && paymentMethodRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException(
                    "Payment method code already exists: " + request.code()
            );
        }

        paymentMethodMapper.updateEntity(paymentMethod, request);

        PaymentMethod updatedPaymentMethod =
                paymentMethodRepository.saveAndFlush(paymentMethod);

        return paymentMethodMapper.toResponse(updatedPaymentMethod);
    }

    @Transactional
    public void delete(UUID id) {
        PaymentMethod paymentMethod = getEntity(id);
        paymentMethodRepository.delete(paymentMethod);
    }

    private PaymentMethod getEntity(UUID id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment method not found: " + id
                ));
    }
}