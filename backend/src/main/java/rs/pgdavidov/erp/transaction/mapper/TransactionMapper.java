package rs.pgdavidov.erp.transaction.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.transaction.dto.TransactionRequest;
import rs.pgdavidov.erp.transaction.dto.TransactionResponse;
import rs.pgdavidov.erp.transaction.dto.TransactionUpdateRequest;
import rs.pgdavidov.erp.transaction.entity.Transaction;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        Transaction transaction = new Transaction();

        transaction.setTransactionCode(request.transactionCode());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setCurrencyCode(request.currencyCode());
        transaction.setDebit(request.debit());
        transaction.setCredit(request.credit());
        transaction.setDescription(request.description());
        transaction.setRawCounterparty(request.rawCounterparty());
        transaction.setBankStatementRowId(request.bankStatementRowId());
        transaction.setReference(request.reference());
        transaction.setStatus(request.status());
        transaction.setSource(request.source());
        transaction.setVerified(request.verified());
        transaction.setNotes(request.notes());

        return transaction;
    }

    public void updateEntity(
            Transaction transaction,
            TransactionUpdateRequest request
    ) {
        transaction.setTransactionDate(request.transactionDate());
        transaction.setCurrencyCode(request.currencyCode());
        transaction.setDebit(request.debit());
        transaction.setCredit(request.credit());
        transaction.setDescription(request.description());
        transaction.setRawCounterparty(request.rawCounterparty());
        transaction.setBankStatementRowId(request.bankStatementRowId());
        transaction.setReference(request.reference());
        transaction.setStatus(request.status());
        transaction.setSource(request.source());
        transaction.setVerified(request.verified());
        transaction.setNotes(request.notes());
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionCode(),
                transaction.getTransactionDate(),
                transaction.getCurrencyCode(),
                transaction.getDebit(),
                transaction.getCredit(),
                transaction.getDescription(),
                transaction.getRawCounterparty(),
                transaction.getBankAccount() != null
                        ? transaction.getBankAccount().getId()
                        : null,
                transaction.getSupplier() != null
                        ? transaction.getSupplier().getId()
                        : null,
                transaction.getCategory() != null
                        ? transaction.getCategory().getId()
                        : null,
                transaction.getBankStatementRowId(),
                transaction.getReference(),
                transaction.getStatus(),
                transaction.getSource(),
                transaction.getVerified(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}