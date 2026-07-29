package rs.pgdavidov.erp.invoice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.pgdavidov.erp.invoice.dto.PaymentRequest;
import rs.pgdavidov.erp.invoice.dto.PaymentResponse;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;
import rs.pgdavidov.erp.invoice.repository.InvoicePaymentRepository;
import rs.pgdavidov.erp.invoice.repository.InvoiceRepository;
import rs.pgdavidov.erp.invoice.service.InvoicePaymentService;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoicePaymentServiceImpl implements InvoicePaymentService {

    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;

    @Override
    public PaymentResponse attachPayment(
            UUID invoiceId,
            PaymentRequest request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void detachPayment(
            UUID invoiceId,
            UUID paymentId
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PaymentResponse> getPayments(UUID invoiceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BigDecimal calculatePaidAmount(UUID invoiceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BigDecimal calculateRemainingAmount(UUID invoiceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InvoiceStatus calculateStatus(UUID invoiceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}