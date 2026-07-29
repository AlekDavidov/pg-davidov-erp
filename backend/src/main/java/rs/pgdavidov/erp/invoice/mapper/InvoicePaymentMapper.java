package rs.pgdavidov.erp.invoice.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.invoice.dto.PaymentResponse;
import rs.pgdavidov.erp.invoice.entity.Invoice;
import rs.pgdavidov.erp.invoice.entity.InvoicePayment;
import rs.pgdavidov.erp.transaction.entity.Transaction;

import java.math.BigDecimal;

@Component
public class InvoicePaymentMapper {

    public PaymentResponse toResponse(InvoicePayment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransaction().getId())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public InvoicePayment toEntity(
            Invoice invoice,
            Transaction transaction,
            BigDecimal amount
    ) {
        return new InvoicePayment(
                invoice,
                transaction,
                amount
        );
    }
}