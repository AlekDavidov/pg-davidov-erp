package rs.pgdavidov.erp.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.pgdavidov.erp.document.entity.Document;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository
        extends JpaRepository<Document, UUID> {

    boolean existsByDocumentCode(String documentCode);

    Optional<Document> findByDocumentCode(String documentCode);

    Optional<Document> findByChecksumSha256(String checksumSha256);

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM transaction_documents
                        WHERE document_id = :documentId
                    )
                    """,
            nativeQuery = true
    )
    boolean existsTransactionDocumentByDocumentId(
            @Param("documentId") UUID documentId
    );
}