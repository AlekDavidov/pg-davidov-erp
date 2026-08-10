package rs.pgdavidov.erp.bankimport.dto;

public record BankImportResultResponse(

        int requestedCount,

        int importedCount,

        int skippedDuplicateCount

) {
}