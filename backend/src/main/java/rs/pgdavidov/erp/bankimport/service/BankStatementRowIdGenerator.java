package rs.pgdavidov.erp.bankimport.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class BankStatementRowIdGenerator {

    public UUID generate(
            String bankCode,
            String accountNumber,
            String statementId,
            Integer entryNumber
    ) {
        String source =
                normalize(bankCode)
                        + "|"
                        + normalize(accountNumber)
                        + "|"
                        + normalize(statementId)
                        + "|"
                        + entryNumber;

        return UUID.nameUUIDFromBytes(
                source.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase();
    }
}