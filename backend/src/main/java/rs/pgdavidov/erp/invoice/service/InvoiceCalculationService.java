package rs.pgdavidov.erp.invoice.service;

import org.springframework.stereotype.Service;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;

import java.math.BigDecimal;

@Service
public class InvoiceCalculationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public BigDecimal calculateRemainingAmount(
            BigDecimal invoiceAmount,
            BigDecimal paidAmount
    ) {
        return invoiceAmount.subtract(paidAmount);
    }

    public InvoiceStatus calculateStatus(
            BigDecimal invoiceAmount,
            BigDecimal paidAmount
    ) {
        if (paidAmount.compareTo(ZERO) == 0) {
            return InvoiceStatus.OPEN;
        }

        int comparison = paidAmount.compareTo(invoiceAmount);

        if (comparison < 0) {
            return InvoiceStatus.PARTIALLY_PAID;
        }

        if (comparison == 0) {
            return InvoiceStatus.PAID;
        }

        return InvoiceStatus.OVERPAID;
    }
}