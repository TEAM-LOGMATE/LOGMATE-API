package com.logmate.dashboard.dto;

import com.logmate.dashboard.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardDto {
    private Long id;
    private String name;
    private String logPath;
    private String sendTo;

    public static DashboardDto from(Dashboard dashboard) {
        return new DashboardDto(
                dashboard.getId(),
                dashboard.getName(),
                dashboard.getLogPath(),
                dashboard.getSendUrl()
        );
    }
}
