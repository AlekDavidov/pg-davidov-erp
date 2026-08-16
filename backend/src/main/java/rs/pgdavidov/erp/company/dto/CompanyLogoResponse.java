package rs.pgdavidov.erp.company.dto;

public record CompanyLogoResponse(

        String filename,

        byte[] content

) {
}