package rs.pgdavidov.erp.bankimport.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BankImportTransactionPreviewResponse(

        Integer entryNumber,

        LocalDate transactionDate,

        LocalDate executionDate,

        BigDecimal debit,

        BigDecimal credit,

        BigDecimal balance,

        String currencyCode,

        String counterparty,

        String counterpartyAccount,

        String description,

        String reference,

        String orderType,

        String orderReference,

        Integer sourcePage,

        UUID supplierId,

        String supplierName,

        UUID categoryId,

        String categoryName,

        String matchStatus,

        boolean duplicate

) {

    public boolean isIncome() {
        return credit != null
                && credit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isExpense() {
        return debit != null
                && debit.compareTo(BigDecimal.ZERO) > 0;
    }
}