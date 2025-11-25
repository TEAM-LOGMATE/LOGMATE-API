package com.logmate.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//대시보드 생성, 수정시 사용
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardRequest {
    private String name;
    private String logPath;
}
