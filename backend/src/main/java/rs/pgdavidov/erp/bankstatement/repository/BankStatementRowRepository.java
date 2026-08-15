package rs.pgdavidov.erp.bankstatement.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.bankstatement.entity.BankStatementRow;

import java.util.Optional;
import java.util.UUID;

public interface BankStatementRowRepository
        extends JpaRepository<BankStatementRow, UUID> {

    boolean existsByBankStatementIdAndEntryNumber(
            UUID bankStatementId,
            Integer entryNumber
    );

    Optional<BankStatementRow>
    findByBankStatementIdAndEntryNumber(
            UUID bankStatementId,
            Integer entryNumber
    );

    @EntityGraph(attributePaths = "bankStatement")
    Optional<BankStatementRow>
    findWithBankStatementById(
            UUID id
    );
}