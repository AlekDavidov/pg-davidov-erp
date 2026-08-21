package rs.pgdavidov.erp.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;
import rs.pgdavidov.erp.bankaccount.repository.BankAccountRepository;
import rs.pgdavidov.erp.dashboard.dto.CashFlowPointResponse;
import rs.pgdavidov.erp.dashboard.dto.DashboardBankAccountResponse;
import rs.pgdavidov.erp.dashboard.dto.DashboardInvoiceResponse;
import rs.pgdavidov.erp.dashboard.dto.DashboardKpiResponse;
import rs.pgdavidov.erp.dashboard.dto.DashboardResponse;
import rs.pgdavidov.erp.dashboard.dto.DashboardTransactionResponse;
import rs.pgdavidov.erp.dashboard.dto.ExpenseCategoryResponse;
import rs.pgdavidov.erp.invoice.entity.Invoice;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;
import rs.pgdavidov.erp.invoice.repository.InvoicePaymentRepository;
import rs.pgdavidov.erp.invoice.repository.InvoiceRepository;
import rs.pgdavidov.erp.invoice.service.InvoiceCalculationService;
import rs.pgdavidov.erp.transaction.entity.Transaction;
import rs.pgdavidov.erp.transaction.entity.TransactionStatus;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private static final String DASHBOARD_CURRENCY =
            "RSD";

    private static final int CASH_FLOW_MONTHS = 12;
    private static final int RECENT_TRANSACTIONS_LIMIT = 10;
    private static final int DUE_INVOICES_LIMIT = 10;

    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final InvoiceCalculationService invoiceCalculationService;

    public DashboardResponse getDashboard(
            int month,
            int year
    ) {
        YearMonth selectedPeriod =
                YearMonth.of(
                        year,
                        month
                );

        LocalDate periodEnd =
                selectedPeriod.atEndOfMonth();

        List<Transaction> transactions =
                transactionRepository
                        .findAll()
                        .stream()
                        .filter(this::isIncludedTransaction)
                        .filter(transaction ->
                                !transaction
                                        .getTransactionDate()
                                        .isAfter(periodEnd)
                        )
                        .toList();

        List<Transaction> financialTransactions =
                transactions
                        .stream()
                        .filter(this::isIncludedInFinancialReport)
                        .toList();

        List<Invoice> invoices =
                invoiceRepository
                        .findAll()
                        .stream()
                        .filter(invoice ->
                                !invoice
                                        .getInvoiceDate()
                                        .isAfter(periodEnd)
                        )
                        .toList();

        List<BankAccount> bankAccounts =
                bankAccountRepository.findAll();

        DashboardKpiResponse income =
                createKpi(
                        financialTransactions,
                        selectedPeriod,
                        Transaction::getCredit
                );

        DashboardKpiResponse expense =
                createKpi(
                        financialTransactions,
                        selectedPeriod,
                        Transaction::getDebit
                );

        List<CashFlowPointResponse> cashFlow =
                createCashFlow(
                        financialTransactions,
                        selectedPeriod
                );

        List<ExpenseCategoryResponse> expenseCategories =
                createExpenseCategories(
                        financialTransactions,
                        selectedPeriod
                );

        List<DashboardTransactionResponse> recentTransactions =
                createRecentTransactions(
                        transactions
                );

        List<DashboardInvoiceResponse> dueInvoices =
                createDueInvoices(
                        invoices,
                        periodEnd
                );

        List<DashboardBankAccountResponse> bankAccountSummaries =
                createBankAccountSummaries(
                        bankAccounts,
                        transactions
                );

        return new DashboardResponse(
                income,
                expense,
                cashFlow,
                expenseCategories,
                recentTransactions,
                dueInvoices,
                bankAccountSummaries
        );
    }

    private DashboardKpiResponse createKpi(
            List<Transaction> transactions,
            YearMonth selectedPeriod,
            Function<Transaction, BigDecimal> amountExtractor
    ) {
        YearMonth previousPeriod =
                selectedPeriod.minusMonths(1);

        BigDecimal currentAmount =
                sumForMonth(
                        transactions,
                        selectedPeriod,
                        amountExtractor
                );

        BigDecimal previousAmount =
                sumForMonth(
                        transactions,
                        previousPeriod,
                        amountExtractor
                );

        BigDecimal difference =
                currentAmount.subtract(
                        previousAmount
                );

        BigDecimal percentageChange =
                calculatePercentageChange(
                        currentAmount,
                        previousAmount
                );

        return new DashboardKpiResponse(
                currentAmount,
                previousAmount,
                difference,
                percentageChange
        );
    }

    private BigDecimal sumForMonth(
            List<Transaction> transactions,
            YearMonth period,
            Function<Transaction, BigDecimal> amountExtractor
    ) {
        return transactions
                .stream()
                .filter(transaction ->
                        DASHBOARD_CURRENCY.equalsIgnoreCase(
                                transaction.getCurrencyCode()
                        )
                )
                .filter(transaction ->
                        YearMonth.from(
                                transaction.getTransactionDate()
                        ).equals(period)
                )
                .map(amountExtractor)
                .filter(amount ->
                        amount != null
                )
                .reduce(
                        ZERO,
                        BigDecimal::add
                );
    }

    private BigDecimal calculatePercentageChange(
            BigDecimal currentAmount,
            BigDecimal previousAmount
    ) {
        if (previousAmount.compareTo(ZERO) == 0) {
            if (currentAmount.compareTo(ZERO) == 0) {
                return ZERO;
            }

            return BigDecimal.valueOf(100);
        }

        return currentAmount
                .subtract(previousAmount)
                .divide(
                        previousAmount,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private List<CashFlowPointResponse> createCashFlow(
            List<Transaction> transactions,
            YearMonth selectedPeriod
    ) {
        YearMonth firstPeriod =
                selectedPeriod.minusMonths(
                        CASH_FLOW_MONTHS - 1L
                );

        Map<YearMonth, MonthlyAmounts> monthlyAmounts =
                new LinkedHashMap<>();

        for (
                int index = 0;
                index < CASH_FLOW_MONTHS;
                index++
        ) {
            monthlyAmounts.put(
                    firstPeriod.plusMonths(index),
                    new MonthlyAmounts()
            );
        }

        transactions
                .stream()
                .filter(transaction ->
                        DASHBOARD_CURRENCY.equalsIgnoreCase(
                                transaction.getCurrencyCode()
                        )
                )
                .forEach(transaction -> {
                    YearMonth transactionPeriod =
                            YearMonth.from(
                                    transaction.getTransactionDate()
                            );

                    MonthlyAmounts amounts =
                            monthlyAmounts.get(
                                    transactionPeriod
                            );

                    if (amounts == null) {
                        return;
                    }

                    amounts.addIncome(
                            transaction.getCredit()
                    );

                    amounts.addExpense(
                            transaction.getDebit()
                    );
                });

        return monthlyAmounts
                .entrySet()
                .stream()
                .map(entry ->
                        new CashFlowPointResponse(
                                entry.getKey(),
                                entry.getValue().income(),
                                entry.getValue().expense()
                        )
                )
                .toList();
    }

    private List<ExpenseCategoryResponse> createExpenseCategories(
            List<Transaction> transactions,
            YearMonth selectedPeriod
    ) {
        Map<UUID, BigDecimal> amountsByCategory =
                transactions
                        .stream()
                        .filter(transaction ->
                                DASHBOARD_CURRENCY.equalsIgnoreCase(
                                        transaction.getCurrencyCode()
                                )
                        )
                        .filter(transaction ->
                                YearMonth.from(
                                        transaction.getTransactionDate()
                                ).equals(selectedPeriod)
                        )
                        .filter(transaction ->
                                transaction.getDebit() != null
                                        && transaction
                                        .getDebit()
                                        .compareTo(ZERO) > 0
                        )
                        .filter(transaction ->
                                transaction.getCategory() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        transaction ->
                                                transaction
                                                        .getCategory()
                                                        .getId(),
                                        Collectors.reducing(
                                                ZERO,
                                                Transaction::getDebit,
                                                BigDecimal::add
                                        )
                                )
                        );

        BigDecimal totalExpense =
                amountsByCategory
                        .values()
                        .stream()
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        return transactions
                .stream()
                .filter(transaction ->
                        transaction.getCategory() != null
                )
                .map(Transaction::getCategory)
                .distinct()
                .filter(category ->
                        amountsByCategory.containsKey(
                                category.getId()
                        )
                )
                .map(category -> {
                    BigDecimal amount =
                            amountsByCategory.get(
                                    category.getId()
                            );

                    Double percentage =
                            calculateCategoryPercentage(
                                    amount,
                                    totalExpense
                            );

                    return new ExpenseCategoryResponse(
                            category.getCode(),
                            category.getName(),
                            amount,
                            percentage
                    );
                })
                .sorted(
                        Comparator.comparing(
                                ExpenseCategoryResponse::amount
                        ).reversed()
                )
                .toList();
    }

    private Double calculateCategoryPercentage(
            BigDecimal amount,
            BigDecimal totalAmount
    ) {
        if (totalAmount.compareTo(ZERO) == 0) {
            return 0D;
        }

        return amount
                .divide(
                        totalAmount,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private List<DashboardTransactionResponse> createRecentTransactions(
            List<Transaction> transactions
    ) {
        return transactions
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Transaction::getTransactionDate
                                )
                                .thenComparing(
                                        Transaction::getCreatedAt
                                )
                                .reversed()
                )
                .limit(
                        RECENT_TRANSACTIONS_LIMIT
                )
                .map(transaction ->
                        new DashboardTransactionResponse(
                                transaction.getId(),
                                transaction.getTransactionCode(),
                                transaction.getTransactionDate(),
                                transaction.getDescription(),
                                transaction.getRawCounterparty(),
                                transaction.getDebit(),
                                transaction.getCredit(),
                                transaction.getCurrencyCode()
                        )
                )
                .toList();
    }

    private List<DashboardInvoiceResponse> createDueInvoices(
            List<Invoice> invoices,
            LocalDate periodEnd
    ) {
        List<DashboardInvoiceResponse> responses =
                new ArrayList<>();

        for (Invoice invoice : invoices) {
            BigDecimal paidAmount =
                    invoicePaymentRepository
                            .sumAmountByInvoiceId(
                                    invoice.getId()
                            );

            BigDecimal remainingAmount =
                    invoiceCalculationService
                            .calculateRemainingAmount(
                                    invoice.getAmount(),
                                    paidAmount
                            );

            InvoiceStatus status =
                    invoiceCalculationService
                            .calculateStatus(
                                    invoice.getAmount(),
                                    paidAmount
                            );

            if (status == InvoiceStatus.PAID
                    || status == InvoiceStatus.OVERPAID) {
                continue;
            }

            String dashboardStatus =
                    resolveInvoiceStatus(
                            status,
                            invoice.getDueDate(),
                            periodEnd
                    );

            responses.add(
                    new DashboardInvoiceResponse(
                            invoice.getId(),
                            invoice.getInvoiceCode(),
                            invoice.getInvoiceNumber(),
                            invoice.getSupplier().getName(),
                            invoice.getDueDate(),
                            invoice.getAmount(),
                            paidAmount,
                            remainingAmount,
                            invoice.getCurrencyCode(),
                            dashboardStatus
                    )
            );
        }

        return responses
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        this::isOverdueInvoice
                                )
                                .reversed()
                                .thenComparing(
                                        DashboardInvoiceResponse::dueDate,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                )
                .limit(
                        DUE_INVOICES_LIMIT
                )
                .toList();
    }

    private String resolveInvoiceStatus(
            InvoiceStatus status,
            LocalDate dueDate,
            LocalDate periodEnd
    ) {
        boolean overdue =
                dueDate != null
                        && dueDate.isBefore(periodEnd);

        if (status == InvoiceStatus.PARTIALLY_PAID) {
            return overdue
                    ? "PARTIALLY_PAID_OVERDUE"
                    : "PARTIALLY_PAID";
        }

        return overdue
                ? "OVERDUE"
                : "OPEN";
    }

    private boolean isOverdueInvoice(
            DashboardInvoiceResponse invoice
    ) {
        return "OVERDUE".equals(
                invoice.status()
        )
                || "PARTIALLY_PAID_OVERDUE".equals(
                invoice.status()
        );
    }

    private List<DashboardBankAccountResponse>
    createBankAccountSummaries(
            List<BankAccount> bankAccounts,
            List<Transaction> transactions
    ) {
        Map<UUID, List<Transaction>> transactionsByAccount =
                transactions
                        .stream()
                        .filter(transaction ->
                                transaction.getBankAccount() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        transaction ->
                                                transaction
                                                        .getBankAccount()
                                                        .getId()
                                )
                        );

        return bankAccounts
                .stream()
                .filter(bankAccount ->
                        Boolean.TRUE.equals(
                                bankAccount.getActive()
                        )
                )
                .sorted(
                        Comparator
                                .comparing(
                                        BankAccount::getBankName
                                )
                                .thenComparing(
                                        BankAccount::getAccountNumber
                                )
                )
                .map(bankAccount -> {
                    List<Transaction> accountTransactions =
                            transactionsByAccount.getOrDefault(
                                    bankAccount.getId(),
                                    List.of()
                            );

                    BigDecimal income =
                            accountTransactions
                                    .stream()
                                    .filter(transaction ->
                                            bankAccount
                                                    .getCurrencyCode()
                                                    .equalsIgnoreCase(
                                                            transaction
                                                                    .getCurrencyCode()
                                                    )
                                    )
                                    .map(
                                            Transaction::getCredit
                                    )
                                    .filter(amount ->
                                            amount != null
                                    )
                                    .reduce(
                                            ZERO,
                                            BigDecimal::add
                                    );

                    BigDecimal expense =
                            accountTransactions
                                    .stream()
                                    .filter(transaction ->
                                            bankAccount
                                                    .getCurrencyCode()
                                                    .equalsIgnoreCase(
                                                            transaction
                                                                    .getCurrencyCode()
                                                    )
                                    )
                                    .map(
                                            Transaction::getDebit
                                    )
                                    .filter(amount ->
                                            amount != null
                                    )
                                    .reduce(
                                            ZERO,
                                            BigDecimal::add
                                    );

                    BigDecimal balance =
                            income.subtract(expense);

                    return new DashboardBankAccountResponse(
                            bankAccount.getId(),
                            bankAccount.getCode(),
                            bankAccount.getBankName(),
                            bankAccount.getAccountNumber(),
                            bankAccount.getCurrencyCode(),
                            balance
                    );
                })
                .toList();
    }

    private boolean isIncludedTransaction(
            Transaction transaction
    ) {
        return transaction.getStatus()
                != TransactionStatus.CANCELLED;
    }

    private boolean isIncludedInFinancialReport(
            Transaction transaction
    ) {
        return transaction.getCategory() == null
                || transaction
                .getCategory()
                .isIncludeInFinancialReport();
    }

    private static final class MonthlyAmounts {

        private BigDecimal income = ZERO;
        private BigDecimal expense = ZERO;

        private void addIncome(
                BigDecimal amount
        ) {
            if (amount != null) {
                income = income.add(amount);
            }
        }

        private void addExpense(
                BigDecimal amount
        ) {
            if (amount != null) {
                expense = expense.add(amount);
            }
        }

        private BigDecimal income() {
            return income;
        }

        private BigDecimal expense() {
            return expense;
        }
    }
}