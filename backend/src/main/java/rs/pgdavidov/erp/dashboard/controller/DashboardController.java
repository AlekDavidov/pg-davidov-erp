package rs.pgdavidov.erp.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.pgdavidov.erp.dashboard.dto.DashboardResponse;
import rs.pgdavidov.erp.dashboard.service.DashboardService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        LocalDate today = LocalDate.now();

        int resolvedMonth =
                month != null
                        ? month
                        : today.getMonthValue();

        int resolvedYear =
                year != null
                        ? year
                        : today.getYear();

        DashboardResponse response =
                dashboardService.getDashboard(
                        resolvedMonth,
                        resolvedYear
                );

        return ResponseEntity.ok(response);
    }
}