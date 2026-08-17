package rs.pgdavidov.erp.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.bankstatement.entity.BankStatementRow;
import rs.pgdavidov.erp.bankstatement.repository.BankStatementRowRepository;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.invoice.entity.Invoice;
import rs.pgdavidov.erp.invoice.entity.InvoicePayment;
import rs.pgdavidov.erp.invoice.repository.InvoicePaymentRepository;
import rs.pgdavidov.erp.invoice.repository.InvoiceRepository;
import rs.pgdavidov.erp.report.dto.SupplierLedgerEntryResponse;
import rs.pgdavidov.erp.report.dto.SupplierLedgerResponse;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;
import rs.pgdavidov.erp.transaction.entity.Transaction;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierLedgerService {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private static final String TYPE_INVOICE =
            "INVOICE";

    private static final String TYPE_PAYMENT =
            "PAYMENT";

    private final SupplierRepository supplierRepository;

    private final InvoiceRepository invoiceRepository;

    private final InvoicePaymentRepository
            invoicePaymentRepository;

    private final TransactionRepository transactionRepository;

    private final BankStatementRowRepository
            bankStatementRowRepository;

    public SupplierLedgerResponse getLedger(
            UUID supplierId,
            LocalDate periodFrom,
            LocalDate periodTo
    ) {
        validatePeriod(
                periodFrom,
                periodTo
        );

        Supplier supplier =
                supplierRepository
                        .findById(supplierId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Supplier with ID '"
                                                + supplierId
                                                + "' was not found."
                                )
                        );

        List<Invoice> invoicesBeforePeriod =
                invoiceRepository
                        .findAllBySupplierIdAndInvoiceDateBeforeOrderByInvoiceDateAsc(
                                supplierId,
                                periodFrom
                        );

        List<InvoicePayment> paymentsBeforePeriod =
                invoicePaymentRepository
                        .findAllBySupplierIdAndTransactionDateBefore(
                                supplierId,
                                periodFrom
                        );

        List<Transaction> directTransactionsBeforePeriod =
                transactionRepository
                        .findUnallocatedSupplierDebitTransactionsBefore(
                                supplierId,
                                periodFrom
                        );

        BigDecimal openingBalance =
                calculateOpeningBalance(
                        invoicesBeforePeriod,
                        paymentsBeforePeriod,
                        directTransactionsBeforePeriod
                );

        List<Invoice> invoicesInPeriod =
                invoiceRepository
                        .findAllBySupplierIdAndInvoiceDateBetweenOrderByInvoiceDateAsc(
                                supplierId,
                                periodFrom,
                                periodTo
                        );

        List<InvoicePayment> paymentsInPeriod =
                invoicePaymentRepository
                        .findAllBySupplierIdAndTransactionDateBetween(
                                supplierId,
                                periodFrom,
                                periodTo
                        );

        List<Transaction> directTransactionsInPeriod =
                transactionRepository
                        .findUnallocatedSupplierDebitTransactionsBetween(
                                supplierId,
                                periodFrom,
                                periodTo
                        );

        List<LedgerEvent> events =
                createEvents(
                        invoicesInPeriod,
                        paymentsInPeriod,
                        directTransactionsInPeriod
                );

        List<SupplierLedgerEntryResponse> entries =
                createEntries(
                        events,
                        openingBalance
                );

        BigDecimal totalInvoiced =
                invoicesInPeriod
                        .stream()
                        .map(Invoice::getAmount)
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        BigDecimal invoicePaymentsTotal =
                paymentsInPeriod
                        .stream()
                        .map(InvoicePayment::getAmount)
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        BigDecimal directPaymentsTotal =
                directTransactionsInPeriod
                        .stream()
                        .map(Transaction::getDebit)
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalPaid =
                invoicePaymentsTotal
                        .add(directPaymentsTotal);

        BigDecimal closingBalance =
                openingBalance
                        .add(totalInvoiced)
                        .subtract(totalPaid);

        String currencyCode =
                resolveCurrencyCode(
                        invoicesBeforePeriod,
                        invoicesInPeriod,
                        paymentsBeforePeriod,
                        paymentsInPeriod,
                        directTransactionsBeforePeriod,
                        directTransactionsInPeriod
                );

        return new SupplierLedgerResponse(
                supplier.getId(),
                supplier.getCode(),
                supplier.getName(),
                supplier.getPib(),
                periodFrom,
                periodTo,
                openingBalance,
                totalInvoiced,
                totalPaid,
                closingBalance,
                currencyCode,
                entries
        );
    }

    private BigDecimal calculateOpeningBalance(
            List<Invoice> invoices,
            List<InvoicePayment> payments,
            List<Transaction> directTransactions
    ) {
        BigDecimal invoicedBeforePeriod =
                invoices
                        .stream()
                        .map(Invoice::getAmount)
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        BigDecimal invoicePaymentsBeforePeriod =
                payments
                        .stream()
                        .map(InvoicePayment::getAmount)
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        BigDecimal directPaymentsBeforePeriod =
                directTransactions
                        .stream()
                        .map(Transaction::getDebit)
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        BigDecimal paidBeforePeriod =
                invoicePaymentsBeforePeriod
                        .add(directPaymentsBeforePeriod);

        return invoicedBeforePeriod
                .subtract(paidBeforePeriod);
    }

    private List<LedgerEvent> createEvents(
            List<Invoice> invoices,
            List<InvoicePayment> payments,
            List<Transaction> directTransactions
    ) {
        List<LedgerEvent> events =
                new ArrayList<>();

        for (Invoice invoice : invoices) {
            events.add(
                    LedgerEvent.forInvoice(
                            invoice
                    )
            );
        }

        for (InvoicePayment payment : payments) {
            events.add(
                    createPaymentEvent(
                            payment
                    )
            );
        }

        for (Transaction transaction : directTransactions) {
            events.add(
                    createDirectPaymentEvent(
                            transaction
                    )
            );
        }

        events.sort(
                Comparator
                        .comparing(
                                LedgerEvent::date
                        )
                        .thenComparingInt(
                                LedgerEvent::sortOrder
                        )
                        .thenComparing(
                                LedgerEvent::id
                        )
        );

        return events;
    }

    private LedgerEvent createPaymentEvent(
            InvoicePayment payment
    ) {
        Transaction transaction =
                payment.getTransaction();

        String statementCode =
                resolveStatementCode(
                        transaction
                );

        return new LedgerEvent(
                payment.getId(),
                transaction.getTransactionDate(),
                TYPE_PAYMENT,
                1,
                payment.getInvoice().getId(),
                null,
                transaction.getId(),
                statementCode,
                transaction.getReference(),
                payment.getAmount(),
                transaction.getCurrencyCode()
        );
    }

    private LedgerEvent createDirectPaymentEvent(
            Transaction transaction
    ) {
        String statementCode =
                resolveStatementCode(
                        transaction
                );

        return new LedgerEvent(
                transaction.getId(),
                transaction.getTransactionDate(),
                TYPE_PAYMENT,
                1,
                null,
                null,
                transaction.getId(),
                statementCode,
                transaction.getReference(),
                transaction.getDebit(),
                transaction.getCurrencyCode()
        );
    }

    private String resolveStatementCode(
            Transaction transaction
    ) {
        UUID bankStatementRowId =
                transaction.getBankStatementRowId();

        if (bankStatementRowId == null) {
            return null;
        }

        return bankStatementRowRepository
                .findWithBankStatementById(
                        bankStatementRowId
                )
                .map(
                        BankStatementRow::getBankStatement
                )
                .map(
                        bankStatement ->
                                bankStatement.getStatementCode()
                )
                .orElse(null);
    }

    private List<SupplierLedgerEntryResponse>
    createEntries(
            List<LedgerEvent> events,
            BigDecimal openingBalance
    ) {
        List<SupplierLedgerEntryResponse> entries =
                new ArrayList<>();

        BigDecimal runningBalance =
                openingBalance;

        for (LedgerEvent event : events) {
            if (
                    TYPE_INVOICE.equals(
                            event.type()
                    )
            ) {
                runningBalance =
                        runningBalance.add(
                                event.amount()
                        );
            } else {
                runningBalance =
                        runningBalance.subtract(
                                event.amount()
                        );
            }

            entries.add(
                    new SupplierLedgerEntryResponse(
                            event.date(),
                            event.type(),
                            event.invoiceId(),
                            event.invoiceNumber(),
                            event.transactionId(),
                            event.statementCode(),
                            event.transactionReference(),
                            TYPE_PAYMENT.equals(
                                    event.type()
                            )
                                    ? event.amount()
                                    : null,
                            TYPE_INVOICE.equals(
                                    event.type()
                            )
                                    ? event.amount()
                                    : null,
                            runningBalance,
                            event.currencyCode()
                    )
            );
        }

        return entries;
    }

    private String resolveCurrencyCode(
            List<Invoice> invoicesBeforePeriod,
            List<Invoice> invoicesInPeriod,
            List<InvoicePayment> paymentsBeforePeriod,
            List<InvoicePayment> paymentsInPeriod,
            List<Transaction> directTransactionsBeforePeriod,
            List<Transaction> directTransactionsInPeriod
    ) {
        List<String> currencyCodes =
                new ArrayList<>();

        invoicesBeforePeriod
                .stream()
                .map(Invoice::getCurrencyCode)
                .forEach(currencyCodes::add);

        invoicesInPeriod
                .stream()
                .map(Invoice::getCurrencyCode)
                .forEach(currencyCodes::add);

        paymentsBeforePeriod
                .stream()
                .map(InvoicePayment::getTransaction)
                .map(Transaction::getCurrencyCode)
                .forEach(currencyCodes::add);

        paymentsInPeriod
                .stream()
                .map(InvoicePayment::getTransaction)
                .map(Transaction::getCurrencyCode)
                .forEach(currencyCodes::add);

        directTransactionsBeforePeriod
                .stream()
                .map(Transaction::getCurrencyCode)
                .forEach(currencyCodes::add);

        directTransactionsInPeriod
                .stream()
                .map(Transaction::getCurrencyCode)
                .forEach(currencyCodes::add);

        List<String> distinctCurrencies =
                currencyCodes
                        .stream()
                        .filter(code ->
                                code != null
                                        && !code.isBlank()
                        )
                        .distinct()
                        .toList();

        if (distinctCurrencies.isEmpty()) {
            return null;
        }

        if (distinctCurrencies.size() > 1) {
            throw new IllegalStateException(
                    "Supplier ledger cannot combine multiple currencies: "
                            + distinctCurrencies
            );
        }

        return distinctCurrencies.getFirst();
    }

    private void validatePeriod(
            LocalDate periodFrom,
            LocalDate periodTo
    ) {
        if (
                periodFrom == null
                        || periodTo == null
        ) {
            throw new IllegalArgumentException(
                    "Period from and period to are required."
            );
        }

        if (
                periodFrom.isAfter(
                        periodTo
                )
        ) {
            throw new IllegalArgumentException(
                    "Period from cannot be after period to."
            );
        }
    }

    private record LedgerEvent(

            UUID id,

            LocalDate date,

            String type,

            int sortOrder,

            UUID invoiceId,

            String invoiceNumber,

            UUID transactionId,

            String statementCode,

            String transactionReference,

            BigDecimal amount,

            String currencyCode

    ) {

        private static LedgerEvent forInvoice(
                Invoice invoice
        ) {
            return new LedgerEvent(
                    invoice.getId(),
                    invoice.getInvoiceDate(),
                    TYPE_INVOICE,
                    0,
                    invoice.getId(),
                    invoice.getInvoiceNumber(),
                    null,
                    null,
                    null,
                    invoice.getAmount(),
                    invoice.getCurrencyCode()
            );
        }
    }
}