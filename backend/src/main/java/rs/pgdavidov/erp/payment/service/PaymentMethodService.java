package rs.pgdavidov.erp.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.common.exception.DuplicateResourceException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
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

    private static final String CODE_PREFIX = "PMT";

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    public List<PaymentMethodResponse> findAll() {
        return paymentMethodRepository.findAll()
                .stream()
                .map(paymentMethodMapper::toResponse)
                .toList();
    }

    public PaymentMethodResponse findById(UUID id) {
        return paymentMethodMapper.toResponse(
                getEntity(id)
        );
    }

    @Transactional
    public PaymentMethodResponse create(
            PaymentMethodRequest request
    ) {
        validateUniqueNameForCreate(request.name());

        String code = generateCode();

        PaymentMethod paymentMethod =
                paymentMethodMapper.toEntity(
                        request,
                        code
                );

        return paymentMethodMapper.toResponse(
                paymentMethodRepository.saveAndFlush(
                        paymentMethod
                )
        );
    }

    @Transactional
    public PaymentMethodResponse update(
            UUID id,
            PaymentMethodRequest request
    ) {
        PaymentMethod paymentMethod = getEntity(id);

        validateUniqueNameForUpdate(
                paymentMethod,
                request.name()
        );

        paymentMethodMapper.updateEntity(
                paymentMethod,
                request
        );

        return paymentMethodMapper.toResponse(
                paymentMethodRepository.saveAndFlush(
                        paymentMethod
                )
        );
    }

    @Transactional
    public void delete(UUID id) {
        paymentMethodRepository.delete(
                getEntity(id)
        );
    }

    private String generateCode() {
        long sequenceValue =
                paymentMethodRepository
                        .getNextCodeSequenceValue();

        return CODE_PREFIX + String.format(
                "%04d",
                sequenceValue
        );
    }

    private void validateUniqueNameForCreate(
            String name
    ) {
        if (paymentMethodRepository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Payment method name already exists: "
                            + name
            );
        }
    }

    private void validateUniqueNameForUpdate(
            PaymentMethod paymentMethod,
            String name
    ) {
        if (!paymentMethod.getName().equals(name)
                && paymentMethodRepository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Payment method name already exists: "
                            + name
            );
        }
    }

    private PaymentMethod getEntity(UUID id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment method not found: "
                                        + id
                        )
                );
    }
}