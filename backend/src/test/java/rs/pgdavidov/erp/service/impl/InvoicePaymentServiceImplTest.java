package rs.pgdavidov.erp.invoice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rs.pgdavidov.erp.common.exception.DuplicateResourceException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.invoice.dto.PaymentRequest;
import rs.pgdavidov.erp.invoice.dto.PaymentResponse;
import rs.pgdavidov.erp.invoice.entity.Invoice;
import rs.pgdavidov.erp.invoice.entity.InvoicePayment;
import rs.pgdavidov.erp.invoice.mapper.InvoicePaymentMapper;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;
import rs.pgdavidov.erp.invoice.repository.InvoicePaymentRepository;
import rs.pgdavidov.erp.invoice.repository.InvoiceRepository;
import rs.pgdavidov.erp.invoice.service.InvoiceCalculationService;
import rs.pgdavidov.erp.transaction.entity.Transaction;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoicePaymentServiceImplTest {

    private static final UUID INVOICE_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID TRANSACTION_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID PAYMENT_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvoicePaymentRepository invoicePaymentRepository;

    @Mock
    private InvoicePaymentMapper invoicePaymentMapper;

    @Mock
    private InvoiceCalculationService invoiceCalculationService;

    @InjectMocks
    private InvoicePaymentServiceImpl invoicePaymentService;

    private Invoice invoice;
    private Transaction transaction;
    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();

        ReflectionTestUtils.setField(
                invoice,
                "id",
                INVOICE_ID
        );

        invoice.setAmount(new BigDecimal("1000.00"));
        invoice.setCurrencyCode("RSD");

        transaction = new Transaction();

        ReflectionTestUtils.setField(
                transaction,
                "id",
                TRANSACTION_ID
        );

        transaction.setDebit(new BigDecimal("1200.00"));
        transaction.setCredit(BigDecimal.ZERO);
        transaction.setCurrencyCode("RSD");

        request = new PaymentRequest();
        request.setTransactionId(TRANSACTION_ID);
        request.setAmount(new BigDecimal("500.00"));
    }

    @Test
    void shouldAttachPayment() {
        InvoicePayment payment = new InvoicePayment(
                invoice,
                transaction,
                request.getAmount()
        );

        PaymentResponse response = PaymentResponse.builder()
                .id(PAYMENT_ID)
                .transactionId(TRANSACTION_ID)
                .amount(request.getAmount())
                .createdAt(
                        OffsetDateTime.parse(
                                "2026-07-29T19:00:00+02:00"
                        )
                )
                .build();

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(transactionRepository.findById(TRANSACTION_ID))
                .thenReturn(Optional.of(transaction));

        when(invoicePaymentRepository
                .existsByInvoice_IdAndTransaction_Id(
                        INVOICE_ID,
                        TRANSACTION_ID
                ))
                .thenReturn(false);

        when(invoicePaymentRepository
                .sumAmountByTransactionId(TRANSACTION_ID))
                .thenReturn(new BigDecimal("200.00"));

        when(invoicePaymentMapper.toEntity(
                invoice,
                transaction,
                request.getAmount()
        )).thenReturn(payment);

        when(invoicePaymentRepository.saveAndFlush(payment))
                .thenReturn(payment);

        when(invoicePaymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result =
                invoicePaymentService.attachPayment(
                        INVOICE_ID,
                        request
                );

        assertEquals(response, result);

        verify(invoicePaymentRepository)
                .saveAndFlush(payment);
    }

    @Test
    void shouldRejectCreditTransaction() {
        transaction.setDebit(BigDecimal.ZERO);
        transaction.setCredit(new BigDecimal("500.00"));

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(transactionRepository.findById(TRANSACTION_ID))
                .thenReturn(Optional.of(transaction));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invoicePaymentService.attachPayment(
                        INVOICE_ID,
                        request
                )
        );

        assertEquals(
                "Only debit transactions can be linked to invoices.",
                exception.getMessage()
        );

        verify(invoicePaymentRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldRejectTransactionWithDifferentCurrency() {
        transaction.setCurrencyCode("EUR");

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(transactionRepository.findById(TRANSACTION_ID))
                .thenReturn(Optional.of(transaction));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invoicePaymentService.attachPayment(
                        INVOICE_ID,
                        request
                )
        );

        assertEquals(
                "Transaction currency must match invoice currency.",
                exception.getMessage()
        );

        verify(invoicePaymentRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldRejectDuplicatePaymentLink() {
        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(transactionRepository.findById(TRANSACTION_ID))
                .thenReturn(Optional.of(transaction));

        when(invoicePaymentRepository
                .existsByInvoice_IdAndTransaction_Id(
                        INVOICE_ID,
                        TRANSACTION_ID
                ))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> invoicePaymentService.attachPayment(
                        INVOICE_ID,
                        request
                )
        );

        assertEquals(
                "Transaction with ID '"
                        + TRANSACTION_ID
                        + "' is already linked to invoice with ID '"
                        + INVOICE_ID
                        + "'.",
                exception.getMessage()
        );

        verify(invoicePaymentRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldRejectAmountGreaterThanAvailableTransactionAmount() {
        request.setAmount(new BigDecimal("500.01"));

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(transactionRepository.findById(TRANSACTION_ID))
                .thenReturn(Optional.of(transaction));

        when(invoicePaymentRepository
                .existsByInvoice_IdAndTransaction_Id(
                        INVOICE_ID,
                        TRANSACTION_ID
                ))
                .thenReturn(false);

        when(invoicePaymentRepository
                .sumAmountByTransactionId(TRANSACTION_ID))
                .thenReturn(new BigDecimal("700.00"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invoicePaymentService.attachPayment(
                        INVOICE_ID,
                        request
                )
        );

        assertEquals(
                "Payment amount exceeds the available transaction amount.",
                exception.getMessage()
        );

        verify(invoicePaymentRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldDetachPayment() {
        InvoicePayment payment = new InvoicePayment(
                invoice,
                transaction,
                new BigDecimal("500.00")
        );

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoicePaymentRepository.findByIdAndInvoice_Id(
                PAYMENT_ID,
                INVOICE_ID
        )).thenReturn(Optional.of(payment));

        invoicePaymentService.detachPayment(
                INVOICE_ID,
                PAYMENT_ID
        );

        verify(invoicePaymentRepository)
                .delete(payment);
    }

    @Test
    void shouldThrowWhenPaymentDoesNotBelongToInvoice() {
        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoicePaymentRepository.findByIdAndInvoice_Id(
                PAYMENT_ID,
                INVOICE_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> invoicePaymentService.detachPayment(
                        INVOICE_ID,
                        PAYMENT_ID
                )
        );

        verify(invoicePaymentRepository, never())
                .delete(any());
    }

    @Test
    void shouldReturnPaymentsForInvoice() {
        InvoicePayment payment = new InvoicePayment(
                invoice,
                transaction,
                new BigDecimal("500.00")
        );

        PaymentResponse response = PaymentResponse.builder()
                .id(PAYMENT_ID)
                .transactionId(TRANSACTION_ID)
                .amount(new BigDecimal("500.00"))
                .build();

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoicePaymentRepository
                .findAllByInvoice_IdOrderByCreatedAtDesc(INVOICE_ID))
                .thenReturn(List.of(payment));

        when(invoicePaymentMapper.toResponse(payment))
                .thenReturn(response);

        List<PaymentResponse> result =
                invoicePaymentService.getPayments(INVOICE_ID);

        assertEquals(List.of(response), result);
    }

    @Test
    void shouldCalculatePaidAmount() {
        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoicePaymentRepository
                .sumAmountByInvoiceId(INVOICE_ID))
                .thenReturn(new BigDecimal("500.00"));

        BigDecimal result =
                invoicePaymentService.calculatePaidAmount(INVOICE_ID);

        assertEquals(
                new BigDecimal("500.00"),
                result
        );
    }

    @Test
    void shouldCalculateRemainingAmount() {
        BigDecimal paidAmount =
                new BigDecimal("500.00");

        BigDecimal remainingAmount =
                new BigDecimal("500.00");

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoicePaymentRepository
                .sumAmountByInvoiceId(INVOICE_ID))
                .thenReturn(paidAmount);

        when(invoiceCalculationService.calculateRemainingAmount(
                invoice.getAmount(),
                paidAmount
        )).thenReturn(remainingAmount);

        BigDecimal result =
                invoicePaymentService.calculateRemainingAmount(INVOICE_ID);

        assertEquals(
                remainingAmount,
                result
        );
    }

    @Test
    void shouldCalculateInvoiceStatus() {
        BigDecimal paidAmount =
                new BigDecimal("500.00");

        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoicePaymentRepository
                .sumAmountByInvoiceId(INVOICE_ID))
                .thenReturn(paidAmount);

        when(invoiceCalculationService.calculateStatus(
                invoice.getAmount(),
                paidAmount
        )).thenReturn(InvoiceStatus.PARTIALLY_PAID);

        InvoiceStatus result =
                invoicePaymentService.calculateStatus(INVOICE_ID);

        assertEquals(
                InvoiceStatus.PARTIALLY_PAID,
                result
        );
    }

    @Test
    void shouldThrowWhenInvoiceDoesNotExist() {
        when(invoiceRepository.findById(INVOICE_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> invoicePaymentService.calculatePaidAmount(
                        INVOICE_ID
                )
        );
    }
}