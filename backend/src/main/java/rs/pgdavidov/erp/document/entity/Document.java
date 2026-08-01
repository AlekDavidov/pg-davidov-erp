package rs.pgdavidov.erp.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "document_code",
            nullable = false,
            unique = true
    )
    private String documentCode;

    @Column(
            name = "display_name",
            nullable = false,
            length = 255
    )
    private String displayName;

    @Column(
            name = "filename",
            nullable = false
    )
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(
            name = "storage_path",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String storagePath;

    @Column(
            name = "checksum_sha256",
            nullable = false
    )
    private String checksumSha256;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;
}