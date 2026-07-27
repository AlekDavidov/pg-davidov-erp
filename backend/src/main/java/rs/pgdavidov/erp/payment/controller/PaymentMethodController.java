package rs.pgdavidov.erp.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.pgdavidov.erp.payment.dto.PaymentMethodRequest;
import rs.pgdavidov.erp.payment.dto.PaymentMethodResponse;
import rs.pgdavidov.erp.payment.service.PaymentMethodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public List<PaymentMethodResponse> findAll() {
        return paymentMethodService.findAll();
    }

    @GetMapping("/{id}")
    public PaymentMethodResponse findById(@PathVariable UUID id) {
        return paymentMethodService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentMethodResponse create(
            @Valid @RequestBody PaymentMethodRequest request
    ) {
        return paymentMethodService.create(request);
    }

    @PutMapping("/{id}")
    public PaymentMethodResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentMethodRequest request
    ) {
        return paymentMethodService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        paymentMethodService.delete(id);
    }
}