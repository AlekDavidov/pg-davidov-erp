package rs.pgdavidov.erp.report.dto;

public record SupplierLedgerExport(

        String filename,

        byte[] content

) {
}