package rs.pgdavidov.erp.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.document.entity.DocumentRecord;

import java.util.UUID;

public interface DocumentRecordRepository
        extends JpaRepository<DocumentRecord, UUID> {
}