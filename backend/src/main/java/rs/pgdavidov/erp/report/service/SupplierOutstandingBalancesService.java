package rs.pgdavidov.erp.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.report.dto.SupplierLedgerResponse;
import rs.pgdavidov.erp.report.dto.SupplierOutstandingBalanceResponse;
import rs.pgdavidov.erp.report.dto.SupplierOutstandingBalancesResponse;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierOutstandingBalancesService {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private final SupplierRepository
            supplierRepository;

    private final SupplierLedgerService
            supplierLedgerService;

    public SupplierOutstandingBalancesResponse getBalances(
            LocalDate periodFrom,
            LocalDate periodTo,
            boolean onlyOutstanding
    ) {
        validatePeriod(
                periodFrom,
                periodTo
        );

        List<SupplierOutstandingBalanceResponse> balances =
                supplierRepository
                        .findAll()
                        .stream()
                        .map(supplier ->
                                createBalance(
                                        supplier,
                                        periodFrom,
                                        periodTo
                                )
                        )
                        .filter(balance ->
                                !onlyOutstanding
                                        || balance.closingBalance()
                                        .compareTo(ZERO) != 0
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                SupplierOutstandingBalanceResponse::closingBalance
                                        )
                                        .reversed()
                                        .thenComparing(
                                                SupplierOutstandingBalanceResponse::supplierName,
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                        )
                        .toList();

        return new SupplierOutstandingBalancesResponse(
                periodFrom,
                periodTo,
                balances
        );
    }

    private SupplierOutstandingBalanceResponse createBalance(
            Supplier supplier,
            LocalDate periodFrom,
            LocalDate periodTo
    ) {
        SupplierLedgerResponse ledger =
                supplierLedgerService.getLedger(
                        supplier.getId(),
                        periodFrom,
                        periodTo
                );

        return new SupplierOutstandingBalanceResponse(
                ledger.supplierId(),
                ledger.supplierCode(),
                ledger.supplierName(),
                ledger.pib(),
                ledger.openingBalance(),
                ledger.totalInvoiced(),
                ledger.totalPaid(),
                ledger.closingBalance(),
                ledger.currencyCode()
        );
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
}