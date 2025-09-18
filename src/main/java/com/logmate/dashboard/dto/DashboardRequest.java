package com.logmate.dashboard.dto;

import lombok.Getter;

//대시보드 생성, 수정시 사용
@Getter
public class DashboardRequest {
    private String name;
    private String logPath;
}
