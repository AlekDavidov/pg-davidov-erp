package rs.pgdavidov.erp.document.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.pgdavidov.erp.document.dto.DocumentDownload;
import rs.pgdavidov.erp.document.dto.DocumentResponse;
import rs.pgdavidov.erp.document.service.DocumentService;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    private static final UUID DOCUMENT_ID =
            UUID.fromString("9a20d918-a958-4e87-a075-812eebd9b90a");

    private static final OffsetDateTime CREATED_AT =
            OffsetDateTime.parse("2026-07-27T15:00:00+02:00");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void shouldUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test-pdf-content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse response = createDocumentResponse();

        when(documentService.upload(
                eq("DOC-2026-0001"),
                any(MockMultipartFile.class)
        )).thenReturn(response);

        mockMvc.perform(
                        multipart("/api/documents")
                                .file(file)
                                .param("documentCode", "DOC-2026-0001")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.documentCode").value("DOC-2026-0001"))
                .andExpect(jsonPath("$.filename").value("invoice.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(16))
                .andExpect(jsonPath("$.storagePath").value(
                        "9a20d918-a958-4e87-a075-812eebd9b90a.pdf"
                ))
                .andExpect(jsonPath("$.checksumSha256").value(
                        "0123456789abcdef0123456789abcdef"
                                + "0123456789abcdef0123456789abcdef"
                ));

        verify(documentService).upload(
                eq("DOC-2026-0001"),
                any(MockMultipartFile.class)
        );
    }

    @Test
    void shouldReturnAllDocuments() throws Exception {
        when(documentService.getAll())
                .thenReturn(List.of(createDocumentResponse()));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$[0].documentCode").value("DOC-2026-0001"))
                .andExpect(jsonPath("$[0].filename").value("invoice.pdf"));

        verify(documentService).getAll();
    }

    @Test
    void shouldReturnDocumentById() throws Exception {
        when(documentService.getById(DOCUMENT_ID))
                .thenReturn(createDocumentResponse());

        mockMvc.perform(get("/api/documents/{id}", DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.documentCode").value("DOC-2026-0001"));

        verify(documentService).getById(DOCUMENT_ID);
    }

    @Test
    void shouldReturnDocumentByCode() throws Exception {
        when(documentService.getByDocumentCode("DOC-2026-0001"))
                .thenReturn(createDocumentResponse());

        mockMvc.perform(
                        get("/api/documents/code/{documentCode}", "DOC-2026-0001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.documentCode").value("DOC-2026-0001"));

        verify(documentService).getByDocumentCode("DOC-2026-0001");
    }

    @Test
    void shouldDownloadDocument() throws Exception {
        byte[] fileContent =
                "test-pdf-content".getBytes(StandardCharsets.UTF_8);

        ByteArrayResource resource = new ByteArrayResource(fileContent);

        DocumentDownload download = new DocumentDownload(
                "invoice.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                (long) fileContent.length,
                resource
        );

        when(documentService.download(DOCUMENT_ID))
                .thenReturn(download);

        mockMvc.perform(
                        get("/api/documents/{id}/download", DOCUMENT_ID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(fileContent))
                .andExpect(header().string(
                        "Content-Disposition",
                        Matchers.containsString("filename=\"invoice.pdf\"")
                ))
                .andExpect(header().longValue(
                        "Content-Length",
                        fileContent.length
                ));

        verify(documentService).download(DOCUMENT_ID);
    }

    @Test
    void shouldDeleteDocument() throws Exception {
        doNothing()
                .when(documentService)
                .delete(DOCUMENT_ID);

        mockMvc.perform(
                        delete("/api/documents/{id}", DOCUMENT_ID)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(documentService).delete(DOCUMENT_ID);
    }

    private DocumentResponse createDocumentResponse() {
        return new DocumentResponse(
                DOCUMENT_ID,
                "DOC-2026-0001",
                "invoice.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                16L,
                "9a20d918-a958-4e87-a075-812eebd9b90a.pdf",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                CREATED_AT
        );
    }
}