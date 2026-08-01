package rs.pgdavidov.erp.dashboard.dto;

import java.math.BigDecimal;

public record ExpenseCategoryResponse(

        String categoryCode,

        String categoryName,

        BigDecimal amount,

        Double percentage

) {
}