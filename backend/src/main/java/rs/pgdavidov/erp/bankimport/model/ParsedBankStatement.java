package rs.pgdavidov.erp.bankimport.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ParsedBankStatement(

        String bankCode,

        String bankName,

        String statementId,

        String statementNumber,

        String accountNumber,

        LocalDate periodFrom,

        LocalDate periodTo,

        String currencyCode,

        BigDecimal openingBalance,

        BigDecimal totalIncome,

        BigDecimal totalExpenses,

        BigDecimal closingBalance,

        List<ParsedBankTransaction> transactions

) {

    public ParsedBankStatement {

        transactions =
                transactions == null
                        ? List.of()
                        : List.copyOf(transactions);

        openingBalance =
                openingBalance != null
                        ? openingBalance
                        : calculateOpeningBalance(
                        transactions
                );

        totalIncome =
                totalIncome != null
                        ? totalIncome
                        : calculateTotalIncome(
                        transactions
                );

        totalExpenses =
                totalExpenses != null
                        ? totalExpenses
                        : calculateTotalExpenses(
                        transactions
                );

        closingBalance =
                closingBalance != null
                        ? closingBalance
                        : calculateClosingBalance(
                        transactions
                );
    }

    public ParsedBankStatement(
            String bankCode,
            String bankName,
            String statementId,
            String statementNumber,
            String accountNumber,
            LocalDate periodFrom,
            LocalDate periodTo,
            String currencyCode,
            List<ParsedBankTransaction> transactions
    ) {
        this(
                bankCode,
                bankName,
                statementId,
                statementNumber,
                accountNumber,
                periodFrom,
                periodTo,
                currencyCode,
                null,
                null,
                null,
                null,
                transactions
        );
    }

    private static BigDecimal calculateOpeningBalance(
            List<ParsedBankTransaction> transactions
    ) {
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        ParsedBankTransaction first =
                transactions.getFirst();

        if (first.balance() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal credit =
                first.credit() != null
                        ? first.credit()
                        : BigDecimal.ZERO;

        BigDecimal debit =
                first.debit() != null
                        ? first.debit()
                        : BigDecimal.ZERO;

        return first
                .balance()
                .subtract(credit)
                .add(debit);
    }

    private static BigDecimal calculateTotalIncome(
            List<ParsedBankTransaction> transactions
    ) {
        return transactions
                .stream()
                .map(
                        ParsedBankTransaction::credit
                )
                .filter(
                        amount ->
                                amount != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private static BigDecimal calculateTotalExpenses(
            List<ParsedBankTransaction> transactions
    ) {
        return transactions
                .stream()
                .map(
                        ParsedBankTransaction::debit
                )
                .filter(
                        amount ->
                                amount != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private static BigDecimal calculateClosingBalance(
            List<ParsedBankTransaction> transactions
    ) {
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance =
                transactions
                        .getLast()
                        .balance();

        return balance != null
                ? balance
                : BigDecimal.ZERO;
    }
}