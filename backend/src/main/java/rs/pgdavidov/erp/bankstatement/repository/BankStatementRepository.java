package rs.pgdavidov.erp.bankstatement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.bankstatement.entity.BankStatement;

import java.util.Optional;
import java.util.UUID;

public interface BankStatementRepository
        extends JpaRepository<BankStatement, UUID> {

    boolean existsByBankAccountIdAndFileChecksumSha256(
            UUID bankAccountId,
            String fileChecksumSha256
    );

    Optional<BankStatement> findByStatementCode(
            String statementCode
    );
}