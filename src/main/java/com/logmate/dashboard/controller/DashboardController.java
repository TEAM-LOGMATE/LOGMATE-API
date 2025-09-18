package com.logmate.dashboard.controller;

import com.logmate.dashboard.service.DashboardService;
import com.logmate.dashboard.dto.DashboardRequest;
import com.logmate.dashboard.dto.DashboardDto;
import com.logmate.global.BaseResponse;
import com.logmate.global.CustomException;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folders/{folderId}/dashboards")
public class DashboardController {
    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<BaseResponse<List<DashboardDto>>> getDashboards(@PathVariable Long folderId,
                                                                          HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<DashboardDto> list = dashboardService.getDashboardsByFolder(folderId, requester);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 목록 조회 성공", list));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DashboardDto>> createDashboard(@PathVariable Long folderId,
                                                                      @RequestBody DashboardRequest request,
                                                                      HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        DashboardDto dashboard = dashboardService.createDashboard(folderId, request, requester);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 생성 성공", dashboard));
    }

    @PutMapping("/{dashboardId}")
    public ResponseEntity<BaseResponse<DashboardDto>> updateDashboard(@PathVariable Long folderId,
                                                                      @PathVariable Long dashboardId,
                                                                      @RequestBody DashboardRequest request,
                                                                      HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        DashboardDto dashboard = dashboardService.updateDashboard(folderId, dashboardId, request, requester);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 수정 성공", dashboard));
    }

    @DeleteMapping("/{dashboardId}")
    public ResponseEntity<BaseResponse<Void>> deleteDashboard(@PathVariable Long folderId,
                                                              @PathVariable Long dashboardId,
                                                              HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        dashboardService.deleteDashboard(folderId, dashboardId, requester);
        return ResponseEntity.ok(BaseResponse.of(200, "대시보드 삭제 성공", null));
    }
}
