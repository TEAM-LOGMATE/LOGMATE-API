package com.logmate.dashboard.dto;

import com.logmate.dashboard.model.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class DashboardDto {
    private Long id;
    private String name;
    private String logPath;
    private String lastModified;
    private Long folderId;
    private String agentId;
    private Integer thNum;

    public static DashboardDto from(Dashboard dashboard, String agentId, Integer thNum) {
        return new DashboardDto(
                dashboard.getId(),
                dashboard.getName(),
                dashboard.getLogPath(),
                dashboard.getUpdatedAt() != null
                        ? dashboard.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        : null,
                dashboard.getFolder().getId(),
                agentId,
                thNum
        );
    }
    public static DashboardDto from(Dashboard dashboard, String agentId) {
        return from(dashboard, agentId, null);
    }

    public static DashboardDto from(Dashboard dashboard) {
        return from(dashboard, null, null);
    }
}
