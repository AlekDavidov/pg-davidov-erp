package rs.pgdavidov.erp.bankimport.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;
import rs.pgdavidov.erp.bankaccount.repository.BankAccountRepository;
import rs.pgdavidov.erp.bankimport.dto.BankImportPreviewResponse;
import rs.pgdavidov.erp.bankimport.dto.BankImportRequest;
import rs.pgdavidov.erp.bankimport.dto.BankImportResultResponse;
import rs.pgdavidov.erp.bankimport.dto.BankImportTransactionPreviewResponse;
import rs.pgdavidov.erp.bankimport.dto.BankImportTransactionRequest;
import rs.pgdavidov.erp.bankimport.dto.SupplierOptionResponse;
import rs.pgdavidov.erp.bankimport.model.ParsedBankStatement;
import rs.pgdavidov.erp.bankimport.model.ParsedBankTransaction;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParser;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParserResolver;
import rs.pgdavidov.erp.bankimport.service.SupplierMatchingService.SupplierMatchResult;
import rs.pgdavidov.erp.bankstatement.entity.BankStatement;
import rs.pgdavidov.erp.bankstatement.entity.BankStatementRow;
import rs.pgdavidov.erp.bankstatement.entity.StatementValidationStatus;
import rs.pgdavidov.erp.bankstatement.entity.SupplierMatchStatus;
import rs.pgdavidov.erp.bankstatement.repository.BankStatementRepository;
import rs.pgdavidov.erp.bankstatement.repository.BankStatementRowRepository;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;
import rs.pgdavidov.erp.transaction.dto.TransactionRequest;
import rs.pgdavidov.erp.transaction.entity.TransactionSource;
import rs.pgdavidov.erp.transaction.entity.TransactionStatus;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;
import rs.pgdavidov.erp.transaction.service.TransactionService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankImportService {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private final BankStatementParserResolver
            parserResolver;

    private final SupplierMatchingService
            supplierMatchingService;

    private final BankStatementRepository
            bankStatementRepository;

    private final BankStatementRowRepository
            bankStatementRowRepository;

    private final BankAccountRepository
            bankAccountRepository;

    private final SupplierRepository
            supplierRepository;

    private final TransactionRepository
            transactionRepository;

    private final TransactionService
            transactionService;

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

        BankStatement existingStatement =
                bankStatementRepository
                        .findByStatementCode(
                                statement.statementId()
                        )
                        .orElse(null);

        List<BankImportTransactionPreviewResponse>
                transactionPreviews =
                statement
                        .transactions()
                        .stream()
                        .map(transaction ->
                                toPreviewResponse(
                                        transaction,
                                        activeSuppliers,
                                        existingStatement
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
                statement.openingBalance(),
                statement.totalIncome(),
                statement.totalExpenses(),
                statement.closingBalance(),
                transactionPreviews.size(),
                transactionPreviews
        );
    }

    @Transactional
    public BankImportResultResponse importTransactions(
            BankImportRequest request
    ) {
        BankAccount bankAccount =
                bankAccountRepository
                        .findById(
                                request.bankAccountId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bank account with ID '"
                                                + request.bankAccountId()
                                                + "' was not found."
                                )
                        );

        BankStatement bankStatement =
                findOrCreateBankStatement(
                        request,
                        bankAccount
                );

        int importedCount = 0;
        int skippedDuplicateCount = 0;

        for (
                BankImportTransactionRequest transaction
                : request.transactions()
        ) {
            BankStatementRow statementRow =
                    findOrCreateStatementRow(
                            bankStatement,
                            transaction
                    );

            boolean duplicate =
                    transactionRepository
                            .existsByBankStatementRowId(
                                    statementRow.getId()
                            );

            if (duplicate) {
                skippedDuplicateCount++;
                continue;
            }

            TransactionRequest transactionRequest =
                    toTransactionRequest(
                            request,
                            transaction,
                            statementRow
                    );

            transactionService.create(
                    transactionRequest
            );

            importedCount++;
        }

        return new BankImportResultResponse(
                request.transactions().size(),
                importedCount,
                skippedDuplicateCount
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

    private BankStatement findOrCreateBankStatement(
            BankImportRequest request,
            BankAccount bankAccount
    ) {
        BankStatement existingStatement =
                bankStatementRepository
                        .findByStatementCode(
                                request.statementId()
                        )
                        .orElse(null);

        if (existingStatement != null) {
            if (
                    !existingStatement
                            .getBankAccount()
                            .getId()
                            .equals(
                                    bankAccount.getId()
                            )
            ) {
                throw new IllegalArgumentException(
                        "Bank statement '"
                                + request.statementId()
                                + "' already belongs to another bank account."
                );
            }

            return existingStatement;
        }

        BankStatement statement =
                new BankStatement();

        statement.setBankAccount(
                bankAccount
        );

        statement.setStatementCode(
                request.statementId()
        );

        statement.setPeriodFrom(
                request.periodFrom()
        );

        statement.setPeriodTo(
                request.periodTo()
        );

        statement.setOpeningBalance(
                request.openingBalance()
        );

        statement.setTotalIncome(
                request.totalIncome()
        );

        statement.setTotalExpenses(
                request.totalExpenses()
        );

        statement.setClosingBalance(
                request.closingBalance()
        );

        statement.setValidationStatus(
                StatementValidationStatus.PENDING
        );

        statement.setOriginalFilename(
                request.originalFilename()
        );

        return bankStatementRepository
                .saveAndFlush(
                        statement
                );
    }

    private BankStatementRow findOrCreateStatementRow(
            BankStatement bankStatement,
            BankImportTransactionRequest transaction
    ) {
        BankStatementRow existingRow =
                bankStatementRowRepository
                        .findByBankStatementIdAndEntryNumber(
                                bankStatement.getId(),
                                transaction.entryNumber()
                        )
                        .orElse(null);

        if (existingRow != null) {
            return existingRow;
        }

        BankStatementRow row =
                new BankStatementRow();

        row.setBankStatement(
                bankStatement
        );

        row.setEntryNumber(
                transaction.entryNumber()
        );

        row.setBookingDate(
                transaction.transactionDate()
        );

        row.setExecutionDate(
                transaction.executionDate()
        );

        row.setIncome(
                resolveAmount(
                        transaction.credit()
                )
        );

        row.setExpenses(
                resolveAmount(
                        transaction.debit()
                )
        );

        row.setCounterpartyRaw(
                transaction.counterparty()
        );

        row.setDescriptionRaw(
                transaction.description()
        );

        row.setOrderType(
                transaction.orderType()
        );

        row.setOrderReference(
                transaction.orderReference()
        );

        row.setComplaintReference(
                transaction.reference()
        );

        row.setPageNumber(
                transaction.sourcePage()
        );

        Supplier supplier =
                findSupplier(
                        transaction.supplierId()
                );

        row.setSuggestedSupplier(
                supplier
        );

        row.setMatchStatus(
                supplier != null
                        ? SupplierMatchStatus.CONFIRMED
                        : SupplierMatchStatus.UNMATCHED
        );

        return bankStatementRowRepository
                .saveAndFlush(
                        row
                );
    }

    private BankImportTransactionPreviewResponse
    toPreviewResponse(
            ParsedBankTransaction transaction,
            List<Supplier> activeSuppliers,
            BankStatement existingStatement
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

        boolean duplicate =
                isDuplicate(
                        existingStatement,
                        transaction.entryNumber()
                );

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
                duplicate
        );
    }

    private boolean isDuplicate(
            BankStatement existingStatement,
            Integer entryNumber
    ) {
        if (existingStatement == null) {
            return false;
        }

        return bankStatementRowRepository
                .findByBankStatementIdAndEntryNumber(
                        existingStatement.getId(),
                        entryNumber
                )
                .map(row ->
                        transactionRepository
                                .existsByBankStatementRowId(
                                        row.getId()
                                )
                )
                .orElse(false);
    }

    private TransactionRequest toTransactionRequest(
            BankImportRequest importRequest,
            BankImportTransactionRequest transaction,
            BankStatementRow statementRow
    ) {
        return new TransactionRequest(
                transaction.transactionDate(),
                transaction.currencyCode(),
                resolveAmount(
                        transaction.debit()
                ),
                resolveAmount(
                        transaction.credit()
                ),
                transaction.description(),
                transaction.counterparty(),
                importRequest.bankAccountId(),
                transaction.supplierId(),
                transaction.categoryId(),
                statementRow.getId(),
                transaction.reference(),
                TransactionStatus.NEW,
                TransactionSource.BANK_IMPORT,
                false,
                null
        );
    }

    private Supplier findSupplier(
            UUID supplierId
    ) {
        if (supplierId == null) {
            return null;
        }

        return supplierRepository
                .findById(
                        supplierId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier with ID '"
                                        + supplierId
                                        + "' was not found."
                        )
                );
    }

    private BigDecimal resolveAmount(
            BigDecimal amount
    ) {
        return amount != null
                ? amount
                : ZERO;
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