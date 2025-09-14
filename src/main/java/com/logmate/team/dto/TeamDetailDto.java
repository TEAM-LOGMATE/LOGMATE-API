package com.logmate.team.dto;

import com.logmate.team.model.Team;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamDetailDto {
    private long id;
    private String name;
    private String description;
    private Long teamFolderId;
    private List<TeamMemberDto> members;

    public TeamDetailDto(Team team, Long teamFolderId, List<TeamMemberDto> members) {
        this.id = team.getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.teamFolderId = teamFolderId;
        this.members = members;
    }
}