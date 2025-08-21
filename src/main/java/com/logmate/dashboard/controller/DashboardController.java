package com.logmate.dashboard.controller;

import com.logmate.dashboard.service.DashboardService;
import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.dto.DashboardDto;
import com.logmate.global.BaseResponse;
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
    public ResponseEntity<BaseResponse<List<DashboardDto>>> getDashboards(@PathVariable Long teamId) {
        List<DashboardDto> list = dashboardService.getDashboardsByTeam(teamId);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 목록 조회 성공", list));
    }

    @PostMapping("/{teamId}/dashboards")
    public ResponseEntity<BaseResponse<DashboardDto>> createDashboard(
            @PathVariable Long teamId,
            @RequestBody DashboardRequest request) {
        DashboardDto dashboard = dashboardService.createDashboard(teamId, request);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 생성 성공", dashboard));
    }

    @PutMapping("/{teamId}/dashboards/{dashboardId}")
    public ResponseEntity<BaseResponse<DashboardDto>> updateDashboard(
            @PathVariable Long teamId,
            @PathVariable Long dashboardId,
            @RequestBody DashboardRequest request) {

        DashboardDto dashboard = dashboardService.updateDashboard(teamId, dashboardId, request);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 수정 성공", dashboard));
    }
}
