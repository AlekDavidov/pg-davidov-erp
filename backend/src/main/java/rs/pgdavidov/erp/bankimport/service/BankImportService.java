package rs.pgdavidov.erp.bankimport.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.dto.BankImportPreviewResponse;
import rs.pgdavidov.erp.bankimport.dto.BankImportTransactionPreviewResponse;
import rs.pgdavidov.erp.bankimport.dto.SupplierOptionResponse;
import rs.pgdavidov.erp.bankimport.model.ParsedBankStatement;
import rs.pgdavidov.erp.bankimport.model.ParsedBankTransaction;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParser;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParserResolver;
import rs.pgdavidov.erp.bankimport.service.SupplierMatchingService.SupplierMatchResult;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankImportService {

    private final BankStatementParserResolver parserResolver;

    private final SupplierMatchingService
            supplierMatchingService;

    public BankImportPreviewResponse preview(
            MultipartFile file
    ) {
        BankStatementParser parser =
                parserResolver.resolve(file);

        ParsedBankStatement statement =
                parser.parse(file);

        List<Supplier> activeSuppliers =
                supplierMatchingService
                        .findActiveSuppliers();

        List<BankImportTransactionPreviewResponse>
                transactionPreviews =
                statement
                        .transactions()
                        .stream()
                        .map(transaction ->
                                toPreviewResponse(
                                        transaction,
                                        activeSuppliers
                                )
                        )
                        .toList();

        return new BankImportPreviewResponse(
                parser.getParserName(),
                statement.bankCode(),
                statement.bankName(),
                statement.statementId(),
                statement.statementNumber(),
                statement.accountNumber(),
                statement.periodFrom(),
                statement.periodTo(),
                statement.currencyCode(),
                transactionPreviews.size(),
                transactionPreviews
        );
    }

    public List<SupplierOptionResponse>
    findSupplierOptions() {
        return supplierMatchingService
                .findActiveSuppliers()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Supplier::getName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(this::toSupplierOptionResponse)
                .toList();
    }

    private BankImportTransactionPreviewResponse
    toPreviewResponse(
            ParsedBankTransaction transaction,
            List<Supplier> activeSuppliers
    ) {
        SupplierMatchResult matchResult =
                supplierMatchingService.match(
                        transaction.counterparty(),
                        activeSuppliers
                );

        Supplier supplier =
                matchResult.supplier();

        Category category =
                supplier != null
                        ? supplier.getDefaultCategory()
                        : null;

        return new BankImportTransactionPreviewResponse(
                transaction.entryNumber(),
                transaction.transactionDate(),
                transaction.executionDate(),
                transaction.debit(),
                transaction.credit(),
                transaction.balance(),
                transaction.currencyCode(),
                transaction.counterparty(),
                transaction.counterpartyAccount(),
                transaction.description(),
                transaction.reference(),
                transaction.orderType(),
                transaction.orderReference(),
                transaction.sourcePage(),
                supplier != null
                        ? supplier.getId()
                        : null,
                supplier != null
                        ? supplier.getName()
                        : null,
                category != null
                        ? category.getId()
                        : null,
                category != null
                        ? category.getName()
                        : null,
                matchResult.status(),
                false
        );
    }

    private SupplierOptionResponse
    toSupplierOptionResponse(
            Supplier supplier
    ) {
        Category defaultCategory =
                supplier.getDefaultCategory();

        return new SupplierOptionResponse(
                supplier.getId(),
                supplier.getCode(),
                supplier.getName(),
                defaultCategory != null
                        ? defaultCategory.getId()
                        : null,
                defaultCategory != null
                        ? defaultCategory.getName()
                        : null
        );
    }
}