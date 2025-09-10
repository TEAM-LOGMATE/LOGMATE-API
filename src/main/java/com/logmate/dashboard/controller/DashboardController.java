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
@RequestMapping("/api/folders/{folderId}/dashboards")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<DashboardDto>>> getDashboards(@PathVariable Long folderId) {
        List<DashboardDto> list = dashboardService.getDashboardsByFolder(folderId);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 목록 조회 성공", list));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DashboardDto>> createDashboard(
            @PathVariable Long folderId,
            @RequestBody DashboardRequest request) {
        DashboardDto dashboard = dashboardService.createDashboard(folderId, request);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 생성 성공", dashboard));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<DashboardDto>> updateDashboard(
            @PathVariable Long folderId,
            @PathVariable Long dashboardId,
            @RequestBody DashboardRequest request) {

        DashboardDto dashboard = dashboardService.updateDashboard(folderId, dashboardId, request);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 수정 성공", dashboard));
    }
}
