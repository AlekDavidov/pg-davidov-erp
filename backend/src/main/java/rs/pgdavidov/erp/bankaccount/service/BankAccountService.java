package rs.pgdavidov.erp.bankaccount.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountRequest;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountResponse;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountUpdateRequest;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;
import rs.pgdavidov.erp.bankaccount.mapper.BankAccountMapper;
import rs.pgdavidov.erp.bankaccount.repository.BankAccountRepository;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankAccountService {

    private static final String CODE_PREFIX = "BAC";

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;

    public List<BankAccountResponse> getAll() {
        return bankAccountRepository
                .findAll(Sort.by(Sort.Direction.ASC, "code"))
                .stream()
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    public BankAccountResponse getById(UUID id) {
        return bankAccountMapper.toResponse(findById(id));
    }

    @Transactional
    public BankAccountResponse create(BankAccountRequest request) {
        String code = generateCode();

        BankAccount bankAccount =
                bankAccountMapper.toEntity(request, code);

        bankAccount.setCurrencyCode(
                normalizeCurrencyCode(request.currencyCode())
        );

        BankAccount savedBankAccount =
                bankAccountRepository.saveAndFlush(bankAccount);

        return bankAccountMapper.toResponse(savedBankAccount);
    }

    @Transactional
    public BankAccountResponse update(
            UUID id,
            BankAccountUpdateRequest request
    ) {
        BankAccount bankAccount = findById(id);

        bankAccountMapper.updateEntity(bankAccount, request);

        bankAccount.setCurrencyCode(
                normalizeCurrencyCode(request.currencyCode())
        );

        BankAccount savedBankAccount =
                bankAccountRepository.saveAndFlush(bankAccount);

        return bankAccountMapper.toResponse(savedBankAccount);
    }

    @Transactional
    public void delete(UUID id) {
        BankAccount bankAccount = findById(id);
        bankAccountRepository.delete(bankAccount);
    }

    private BankAccount findById(UUID id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account with ID '" + id + "' was not found."
                ));
    }

    private String generateCode() {
        long sequenceValue =
                bankAccountRepository.getNextCodeSequenceValue();

        return CODE_PREFIX + String.format("%04d", sequenceValue);
    }

    private String normalizeCurrencyCode(String currencyCode) {
        return currencyCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}