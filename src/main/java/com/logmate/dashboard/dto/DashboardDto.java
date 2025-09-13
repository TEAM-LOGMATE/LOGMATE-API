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
    private String sendTo;
    private String lastModified;
    private Long folderId;

    public static DashboardDto from(Dashboard dashboard) {
        return new DashboardDto(
                dashboard.getId(),
                dashboard.getName(),
                dashboard.getLogPath(),
                dashboard.getSendUrl(),
                dashboard.getUpdatedAt() != null
                        ? dashboard.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        : null,
                dashboard.getFolder().getId()
        );
    }
}
