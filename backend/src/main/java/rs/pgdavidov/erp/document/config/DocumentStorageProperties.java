package rs.pgdavidov.erp.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.document-storage")
@Getter
@Setter
public class DocumentStorageProperties {

    private String location = "./storage/documents";

    private long maxFileSizeBytes = 10 * 1024 * 1024;
}