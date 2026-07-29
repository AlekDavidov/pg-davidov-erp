package rs.pgdavidov.erp.invoice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import rs.pgdavidov.erp.invoice.service.InvoicePaymentService;
import rs.pgdavidov.erp.transaction.entity.Transaction;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoicePaymentServiceImpl implements InvoicePaymentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final InvoicePaymentMapper invoicePaymentMapper;
    private final InvoiceCalculationService invoiceCalculationService;

    @Override
    @Transactional
    public PaymentResponse attachPayment(
            UUID invoiceId,
            PaymentRequest request
    ) {
        Invoice invoice = findInvoiceById(invoiceId);

        Transaction transaction =
                findTransactionById(request.getTransactionId());

        validateTransactionIsDebit(transaction);
        validateCurrency(invoice, transaction);
        validateDuplicatePayment(invoiceId, transaction.getId());
        validateAvailableTransactionAmount(
                transaction,
                request.getAmount()
        );

        InvoicePayment payment =
                invoicePaymentMapper.toEntity(
                        invoice,
                        transaction,
                        request.getAmount()
                );

        InvoicePayment savedPayment =
                invoicePaymentRepository.saveAndFlush(payment);

        return invoicePaymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional
    public void detachPayment(
            UUID invoiceId,
            UUID paymentId
    ) {
        findInvoiceById(invoiceId);

        InvoicePayment payment =
                invoicePaymentRepository
                        .findByIdAndInvoice_Id(
                                paymentId,
                                invoiceId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invoice payment with ID '"
                                                + paymentId
                                                + "' was not found for invoice with ID '"
                                                + invoiceId
                                                + "'."
                                )
                        );

        invoicePaymentRepository.delete(payment);
    }

    @Override
    public List<PaymentResponse> getPayments(UUID invoiceId) {
        findInvoiceById(invoiceId);

        return invoicePaymentRepository
                .findAllByInvoice_IdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(invoicePaymentMapper::toResponse)
                .toList();
    }

    @Override
    public BigDecimal calculatePaidAmount(UUID invoiceId) {
        findInvoiceById(invoiceId);

        return invoicePaymentRepository
                .sumAmountByInvoiceId(invoiceId);
    }

    @Override
    public BigDecimal calculateRemainingAmount(UUID invoiceId) {
        Invoice invoice = findInvoiceById(invoiceId);

        BigDecimal paidAmount =
                invoicePaymentRepository
                        .sumAmountByInvoiceId(invoiceId);

        return invoiceCalculationService
                .calculateRemainingAmount(
                        invoice.getAmount(),
                        paidAmount
                );
    }

    @Override
    public InvoiceStatus calculateStatus(UUID invoiceId) {
        Invoice invoice = findInvoiceById(invoiceId);

        BigDecimal paidAmount =
                invoicePaymentRepository
                        .sumAmountByInvoiceId(invoiceId);

        return invoiceCalculationService.calculateStatus(
                invoice.getAmount(),
                paidAmount
        );
    }

    private Invoice findInvoiceById(UUID invoiceId) {
        return invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice with ID '"
                                        + invoiceId
                                        + "' was not found."
                        )
                );
    }

    private Transaction findTransactionById(UUID transactionId) {
        return transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction with ID '"
                                        + transactionId
                                        + "' was not found."
                        )
                );
    }

    private void validateTransactionIsDebit(
            Transaction transaction
    ) {
        if (transaction.getDebit() == null
                || transaction.getDebit().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Only debit transactions can be linked to invoices."
            );
        }
    }

    private void validateCurrency(
            Invoice invoice,
            Transaction transaction
    ) {
        if (!invoice.getCurrencyCode().equalsIgnoreCase(
                transaction.getCurrencyCode()
        )) {
            throw new IllegalArgumentException(
                    "Transaction currency must match invoice currency."
            );
        }
    }

    private void validateDuplicatePayment(
            UUID invoiceId,
            UUID transactionId
    ) {
        boolean alreadyLinked =
                invoicePaymentRepository
                        .existsByInvoice_IdAndTransaction_Id(
                                invoiceId,
                                transactionId
                        );

        if (alreadyLinked) {
            throw new DuplicateResourceException(
                    "Transaction with ID '"
                            + transactionId
                            + "' is already linked to invoice with ID '"
                            + invoiceId
                            + "'."
            );
        }
    }

    private void validateAvailableTransactionAmount(
            Transaction transaction,
            BigDecimal requestedAmount
    ) {
        BigDecimal alreadyAllocatedAmount =
                invoicePaymentRepository
                        .sumAmountByTransactionId(
                                transaction.getId()
                        );

        BigDecimal availableAmount =
                transaction
                        .getDebit()
                        .subtract(alreadyAllocatedAmount);

        if (requestedAmount.compareTo(availableAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount exceeds the available transaction amount."
            );
        }
    }
}