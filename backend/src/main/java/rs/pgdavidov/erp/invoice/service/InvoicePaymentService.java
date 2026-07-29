package rs.pgdavidov.erp.invoice.service;

import rs.pgdavidov.erp.invoice.dto.PaymentRequest;
import rs.pgdavidov.erp.invoice.dto.PaymentResponse;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InvoicePaymentService {

    PaymentResponse attachPayment(UUID invoiceId, PaymentRequest request);

    void detachPayment(UUID invoiceId, UUID paymentId);

    List<PaymentResponse> getPayments(UUID invoiceId);

    BigDecimal calculatePaidAmount(UUID invoiceId);

    BigDecimal calculateRemainingAmount(UUID invoiceId);

    InvoiceStatus calculateStatus(UUID invoiceId);

}