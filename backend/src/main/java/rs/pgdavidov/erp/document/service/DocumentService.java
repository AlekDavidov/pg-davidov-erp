package rs.pgdavidov.erp.document.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.common.exception.DocumentStorageException;
import rs.pgdavidov.erp.common.exception.ResourceNotFoundException;
import rs.pgdavidov.erp.document.config.DocumentStorageProperties;
import rs.pgdavidov.erp.document.dto.DocumentDownload;
import rs.pgdavidov.erp.document.dto.DocumentResponse;
import rs.pgdavidov.erp.document.entity.Document;
import rs.pgdavidov.erp.document.mapper.DocumentMapper;
import rs.pgdavidov.erp.document.repository.DocumentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private static final String CODE_PREFIX = "DOC-";

    private static final int MAX_DISPLAY_NAME_LENGTH = 255;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentStorageProperties storageProperties;
    private final Path storageRoot;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentMapper documentMapper,
            DocumentStorageProperties storageProperties
    ) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.storageProperties = storageProperties;
        this.storageRoot = Path.of(storageProperties.getLocation())
                .toAbsolutePath()
                .normalize();

        createStorageDirectory();
    }

    /*
     * Privremeno zadržano radi kompatibilnosti sa postojećim
     * DocumentController kodom.
     *
     * Kada controller počne da šalje displayName, koristiće se
     * overload metoda upload(String, MultipartFile).
     */
    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        String fallbackDisplayName =
                createFallbackDisplayName(file);

        return upload(
                fallbackDisplayName,
                file
        );
    }

    @Transactional
    public DocumentResponse upload(
            String displayName,
            MultipartFile file
    ) {
        Document document = createDocument(
                displayName,
                file
        );

        return documentMapper.toResponse(document);
    }

    @Transactional
    public Document createDocument(
            String displayName,
            MultipartFile file
    ) {
        validateFile(file);

        String normalizedDisplayName =
                normalizeDisplayName(displayName);

        String documentCode =
                generateDocumentCode();

        String originalFilename =
                sanitizeFilename(
                        file.getOriginalFilename()
                );

        String contentType =
                normalizeContentType(
                        file.getContentType()
                );

        String storedFilename =
                createStoredFilename(
                        originalFilename
                );

        Path targetPath =
                resolveStoragePath(
                        storedFilename
                );

        String checksum =
                calculateChecksum(file);

        saveFile(
                file,
                targetPath
        );

        Document document = new Document();

        document.setDocumentCode(documentCode);
        document.setDisplayName(normalizedDisplayName);
        document.setFilename(originalFilename);
        document.setContentType(contentType);
        document.setSizeBytes(file.getSize());
        document.setStoragePath(storedFilename);
        document.setChecksumSha256(checksum);

        try {
            return documentRepository
                    .saveAndFlush(document);
        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(targetPath);

            throw exception;
        }
    }

    public List<DocumentResponse> getAll() {
        return documentRepository
                .findAll()
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    public DocumentResponse getById(UUID id) {
        return documentMapper.toResponse(
                findDocument(id)
        );
    }

    public DocumentResponse getByDocumentCode(
            String documentCode
    ) {
        String normalizedDocumentCode =
                normalizeDocumentCode(documentCode);

        Document document = documentRepository
                .findByDocumentCode(
                        normalizedDocumentCode
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document with code '"
                                        + normalizedDocumentCode
                                        + "' was not found."
                        )
                );

        return documentMapper.toResponse(document);
    }

    public DocumentDownload download(UUID id) {
        Document document = findDocument(id);

        Path filePath =
                resolveStoragePath(
                        document.getStoragePath()
                );

        if (!Files.exists(filePath)
                || !Files.isRegularFile(filePath)) {
            throw new DocumentStorageException(
                    "Stored file for document '"
                            + document.getDocumentCode()
                            + "' was not found."
            );
        }

        try {
            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {
                throw new DocumentStorageException(
                        "Stored file for document '"
                                + document.getDocumentCode()
                                + "' is not readable."
                );
            }

            return new DocumentDownload(
                    document.getFilename(),
                    document.getContentType(),
                    document.getSizeBytes(),
                    resource
            );
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Stored file for document '"
                            + document.getDocumentCode()
                            + "' could not be loaded.",
                    exception
            );
        }
    }

    @Transactional
    public void delete(UUID id) {
        Document document = findDocument(id);

        deleteDocument(document);
    }

    @Transactional
    public void deleteDocument(Document document) {
        Path filePath =
                resolveStoragePath(
                        document.getStoragePath()
                );

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Stored file for document '"
                            + document.getDocumentCode()
                            + "' could not be deleted.",
                    exception
            );
        }

        documentRepository.delete(document);
        documentRepository.flush();
    }

    public Document findEntityById(UUID id) {
        return findDocument(id);
    }

    public DocumentResponse toResponse(
            Document document
    ) {
        return documentMapper.toResponse(document);
    }

    private Document findDocument(UUID id) {
        return documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document with ID '"
                                        + id
                                        + "' was not found."
                        )
                );
    }

    private String generateDocumentCode() {
        String generatedCode;

        do {
            long sequenceValue =
                    documentRepository
                            .getNextCodeSequenceValue();

            generatedCode =
                    CODE_PREFIX
                            + String.format(
                            "%06d",
                            sequenceValue
                    );
        } while (
                documentRepository
                        .existsByDocumentCode(
                                generatedCode
                        )
        );

        return generatedCode;
    }

    private String normalizeDisplayName(
            String displayName
    ) {
        if (displayName == null
                || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "Document display name is required."
            );
        }

        String normalizedDisplayName =
                displayName.trim();

        if (normalizedDisplayName.length()
                > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Document display name cannot exceed "
                            + MAX_DISPLAY_NAME_LENGTH
                            + " characters."
            );
        }

        return normalizedDisplayName;
    }

    private String createFallbackDisplayName(
            MultipartFile file
    ) {
        if (file == null) {
            return null;
        }

        return sanitizeFilename(
                file.getOriginalFilename()
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Document file is required."
            );
        }

        if (file.getSize()
                > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException(
                    "Document file exceeds the maximum allowed size of "
                            + storageProperties
                            .getMaxFileSizeBytes()
                            + " bytes."
            );
        }

        String contentType =
                normalizeContentType(
                        file.getContentType()
                );

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported document content type '"
                            + contentType
                            + "'."
            );
        }
    }

    private String normalizeDocumentCode(
            String documentCode
    ) {
        if (documentCode == null) {
            return null;
        }

        return documentCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeContentType(
            String contentType
    ) {
        if (contentType == null
                || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String sanitizeFilename(
            String originalFilename
    ) {
        if (originalFilename == null
                || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "Document filename is required."
            );
        }

        String sanitizedFilename =
                Path.of(originalFilename)
                        .getFileName()
                        .toString()
                        .trim();

        if (sanitizedFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "Document filename is invalid."
            );
        }

        return sanitizedFilename;
    }

    private String createStoredFilename(
            String originalFilename
    ) {
        return UUID.randomUUID()
                + extractExtension(
                originalFilename
        );
    }

    private String extractExtension(
            String filename
    ) {
        int lastDotIndex =
                filename.lastIndexOf('.');

        if (lastDotIndex < 0
                || lastDotIndex
                == filename.length() - 1) {
            return "";
        }

        return filename
                .substring(lastDotIndex)
                .toLowerCase(Locale.ROOT);
    }

    private String calculateChecksum(
            MultipartFile file
    ) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            try (
                    InputStream inputStream =
                            file.getInputStream()
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while (
                        (bytesRead =
                                inputStream.read(buffer))
                                != -1
                ) {
                    messageDigest.update(
                            buffer,
                            0,
                            bytesRead
                    );
                }
            }

            return HexFormat
                    .of()
                    .formatHex(
                            messageDigest.digest()
                    );
        } catch (
                NoSuchAlgorithmException exception
        ) {
            throw new DocumentStorageException(
                    "SHA-256 checksum algorithm is unavailable.",
                    exception
            );
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Document checksum could not be calculated.",
                    exception
            );
        }
    }

    private void saveFile(
            MultipartFile file,
            Path targetPath
    ) {
        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {
            Files.copy(
                    inputStream,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Document file could not be stored.",
                    exception
            );
        }
    }

    private Path resolveStoragePath(
            String storedFilename
    ) {
        Path resolvedPath =
                storageRoot
                        .resolve(storedFilename)
                        .normalize();

        if (!resolvedPath.startsWith(storageRoot)) {
            throw new DocumentStorageException(
                    "Document storage path is invalid."
            );
        }

        return resolvedPath;
    }

    private void createStorageDirectory() {
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Document storage directory could not be created.",
                    exception
            );
        }
    }

    private void deleteStoredFileQuietly(
            Path filePath
    ) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Original persistence exception remains primary.
        }
    }
}