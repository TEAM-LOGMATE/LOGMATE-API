package com.logmate.dashboard;

import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.dto.DashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/{teamId}/dashboards")
    public ResponseEntity<?> getDashboards(@PathVariable Long teamId) {
        List<DashboardDto> list = dashboardService.getDashboardsByTeam(teamId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{teamId}/dashboards")
    public ResponseEntity<DashboardDto> createDashboard(
            @PathVariable Long teamId,
            @RequestBody DashboardRequest request) {
        DashboardDto dashboard = dashboardService.createDashboard(teamId, request);
        return ResponseEntity.ok(dashboard);
    }

    @PutMapping("/{teamId}/dashboards/{dashboardId}")
    public ResponseEntity<DashboardDto> updateDashboard(
            @PathVariable Long teamId,
            @PathVariable Long dashboardId,
            @RequestBody DashboardRequest request) {

        DashboardDto dashboard = dashboardService.updateDashboard(teamId, dashboardId, request);
        return ResponseEntity.ok(dashboard);
    }
}
