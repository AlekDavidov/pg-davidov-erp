package rs.pgdavidov.erp.category.dto;

import java.util.UUID;

public record CategoryOptionResponse(

        UUID id,

        String code,

        String name

) {
}