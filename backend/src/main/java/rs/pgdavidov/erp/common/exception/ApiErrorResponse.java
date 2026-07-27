package rs.pgdavidov.erp.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
}