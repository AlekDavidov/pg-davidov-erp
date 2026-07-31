package rs.pgdavidov.erp.bankaccount.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.pgdavidov.erp.bankaccount.entity.BankAccount;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, UUID> {

    Optional<BankAccount> findByCode(String code);

    boolean existsByCode(String code);

    @Query(
            value = "SELECT nextval('bank_account_code_seq')",
            nativeQuery = true
    )
    long getNextCodeSequenceValue();
}