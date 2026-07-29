package rs.pgdavidov.erp.invoice.service;

import org.junit.jupiter.api.Test;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceCalculationServiceTest {

    private final InvoiceCalculationService invoiceCalculationService =
            new InvoiceCalculationService();

    @Test
    void shouldCalculateRemainingAmount() {
        BigDecimal result =
                invoiceCalculationService.calculateRemainingAmount(
                        new BigDecimal("1000.00"),
                        new BigDecimal("250.00")
                );

        assertEquals(new BigDecimal("750.00"), result);
    }

    @Test
    void shouldReturnOpenStatusWhenNothingIsPaid() {
        InvoiceStatus result =
                invoiceCalculationService.calculateStatus(
                        new BigDecimal("1000.00"),
                        BigDecimal.ZERO
                );

        assertEquals(InvoiceStatus.OPEN, result);
    }

    @Test
    void shouldReturnPartiallyPaidStatus() {
        InvoiceStatus result =
                invoiceCalculationService.calculateStatus(
                        new BigDecimal("1000.00"),
                        new BigDecimal("250.00")
                );

        assertEquals(InvoiceStatus.PARTIALLY_PAID, result);
    }

    @Test
    void shouldReturnPaidStatus() {
        InvoiceStatus result =
                invoiceCalculationService.calculateStatus(
                        new BigDecimal("1000.00"),
                        new BigDecimal("1000.00")
                );

        assertEquals(InvoiceStatus.PAID, result);
    }

    @Test
    void shouldReturnOverpaidStatus() {
        InvoiceStatus result =
                invoiceCalculationService.calculateStatus(
                        new BigDecimal("1000.00"),
                        new BigDecimal("1200.00")
                );

        assertEquals(InvoiceStatus.OVERPAID, result);
    }
}
