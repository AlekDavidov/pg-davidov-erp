package rs.pgdavidov.erp.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;
import rs.pgdavidov.erp.bankaccount.repository.BankAccountRepository;
import rs.pgdavidov.erp.category.entity.Category;
import rs.pgdavidov.erp.category.repository.CategoryRepository;
import rs.pgdavidov.erp.common.exception.DuplicateResourceException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;
import rs.pgdavidov.erp.transaction.dto.TransactionRequest;
import rs.pgdavidov.erp.transaction.dto.TransactionResponse;
import rs.pgdavidov.erp.transaction.dto.TransactionUpdateRequest;
import rs.pgdavidov.erp.transaction.entity.Transaction;
import rs.pgdavidov.erp.transaction.mapper.TransactionMapper;
import rs.pgdavidov.erp.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String CODE_PREFIX = "TRX-";

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public List<TransactionResponse> getAll() {
        return transactionRepository
                .findAll(
                        Sort.by(
                                Sort.Order.desc("transactionDate"),
                                Sort.Order.desc("createdAt")
                        )
                )
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    public TransactionResponse getById(UUID id) {
        return transactionMapper.toResponse(findById(id));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        validateAmounts(
                request.debit(),
                request.credit()
        );

        validateBankStatementRowForCreate(
                request.bankStatementRowId()
        );

        String transactionCode =
                generateTransactionCode();

        Transaction transaction =
                transactionMapper.toEntity(
                        request,
                        transactionCode
                );

        transaction.setCurrencyCode(
                normalizeCurrencyCode(
                        request.currencyCode()
                )
        );

        setRelations(
                transaction,
                request.bankAccountId(),
                request.supplierId(),
                request.categoryId()
        );

        Transaction savedTransaction =
                transactionRepository.saveAndFlush(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse update(
            UUID id,
            TransactionUpdateRequest request
    ) {
        Transaction transaction = findById(id);

        validateAmounts(
                request.debit(),
                request.credit()
        );

        validateBankStatementRowForUpdate(
                request.bankStatementRowId(),
                transaction.getId()
        );

        transactionMapper.updateEntity(
                transaction,
                request
        );

        transaction.setCurrencyCode(
                normalizeCurrencyCode(
                        request.currencyCode()
                )
        );

        setRelations(
                transaction,
                request.bankAccountId(),
                request.supplierId(),
                request.categoryId()
        );

        Transaction savedTransaction =
                transactionRepository.saveAndFlush(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public void delete(UUID id) {
        Transaction transaction = findById(id);
        transactionRepository.delete(transaction);
    }

    private Transaction findById(UUID id) {
        return transactionRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction with ID '"
                                        + id
                                        + "' was not found."
                        )
                );
    }

    private void setRelations(
            Transaction transaction,
            UUID bankAccountId,
            UUID supplierId,
            UUID categoryId
    ) {
        transaction.setBankAccount(
                findBankAccount(bankAccountId)
        );

        transaction.setSupplier(
                findSupplier(supplierId)
        );

        transaction.setCategory(
                findCategory(categoryId)
        );
    }

    private BankAccount findBankAccount(UUID bankAccountId) {
        if (bankAccountId == null) {
            return null;
        }

        return bankAccountRepository
                .findById(bankAccountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank account with ID '"
                                        + bankAccountId
                                        + "' was not found."
                        )
                );
    }

    private Supplier findSupplier(UUID supplierId) {
        if (supplierId == null) {
            return null;
        }

        return supplierRepository
                .findById(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier with ID '"
                                        + supplierId
                                        + "' was not found."
                        )
                );
    }

    private Category findCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category with ID '"
                                        + categoryId
                                        + "' was not found."
                        )
                );
    }

    private String generateTransactionCode() {
        long sequenceValue =
                transactionRepository
                        .getNextCodeSequenceValue();

        return CODE_PREFIX + String.format(
                "%04d",
                sequenceValue
        );
    }

    private void validateAmounts(
            BigDecimal debit,
            BigDecimal credit
    ) {
        if (debit.compareTo(ZERO) < 0
                || credit.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Debit and credit amounts cannot be negative."
            );
        }

        boolean debitIsPositive =
                debit.compareTo(ZERO) > 0;

        boolean creditIsPositive =
                credit.compareTo(ZERO) > 0;

        if (debitIsPositive == creditIsPositive) {
            throw new IllegalArgumentException(
                    "Exactly one of debit or credit must be greater than zero."
            );
        }
    }

    private void validateBankStatementRowForCreate(
            UUID bankStatementRowId
    ) {
        if (bankStatementRowId == null) {
            return;
        }

        if (transactionRepository
                .existsByBankStatementRowId(
                        bankStatementRowId
                )) {
            throw new DuplicateResourceException(
                    "Transaction for bank statement row ID '"
                            + bankStatementRowId
                            + "' already exists."
            );
        }
    }

    private void validateBankStatementRowForUpdate(
            UUID bankStatementRowId,
            UUID transactionId
    ) {
        if (bankStatementRowId == null) {
            return;
        }

        if (transactionRepository
                .existsByBankStatementRowIdAndIdNot(
                        bankStatementRowId,
                        transactionId
                )) {
            throw new DuplicateResourceException(
                    "Transaction for bank statement row ID '"
                            + bankStatementRowId
                            + "' already exists."
            );
        }
    }

    private String normalizeCurrencyCode(String currencyCode) {
        return currencyCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}