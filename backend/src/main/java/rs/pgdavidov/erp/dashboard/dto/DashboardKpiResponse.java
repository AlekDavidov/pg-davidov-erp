package rs.pgdavidov.erp.dashboard.dto;

import java.math.BigDecimal;

public record DashboardKpiResponse(

        BigDecimal currentAmount,

        BigDecimal previousAmount,

        BigDecimal difference,

        BigDecimal percentageChange

) {
}