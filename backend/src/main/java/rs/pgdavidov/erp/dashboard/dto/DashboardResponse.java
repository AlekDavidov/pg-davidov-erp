package rs.pgdavidov.erp.dashboard.dto;

import java.util.List;

public record DashboardResponse(

        DashboardKpiResponse income,

        DashboardKpiResponse expense,

        List<CashFlowPointResponse> cashFlow,

        List<ExpenseCategoryResponse> expenseCategories,

        List<DashboardTransactionResponse> recentTransactions,

        List<DashboardInvoiceResponse> dueInvoices,

        List<DashboardBankAccountResponse> bankAccounts

) {
}