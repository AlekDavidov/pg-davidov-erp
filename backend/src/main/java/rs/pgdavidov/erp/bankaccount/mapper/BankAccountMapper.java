package rs.pgdavidov.erp.bankaccount.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountRequest;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountResponse;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountUpdateRequest;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;

@Component
public class BankAccountMapper {

    public BankAccount toEntity(
            BankAccountRequest request,
            String code
    ) {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setCode(code);
        bankAccount.setBankName(request.bankName());
        bankAccount.setAccountNumber(request.accountNumber());
        bankAccount.setCurrencyCode(request.currencyCode());
        bankAccount.setActive(request.active());

        return bankAccount;
    }

    public void updateEntity(
            BankAccount bankAccount,
            BankAccountUpdateRequest request
    ) {
        bankAccount.setBankName(request.bankName());
        bankAccount.setAccountNumber(request.accountNumber());
        bankAccount.setCurrencyCode(request.currencyCode());
        bankAccount.setActive(request.active());
    }

    public BankAccountResponse toResponse(BankAccount bankAccount) {
        return new BankAccountResponse(
                bankAccount.getId(),
                bankAccount.getCode(),
                bankAccount.getBankName(),
                bankAccount.getAccountNumber(),
                bankAccount.getCurrencyCode(),
                bankAccount.getActive(),
                bankAccount.getCreatedAt(),
                bankAccount.getUpdatedAt()
        );
    }
}