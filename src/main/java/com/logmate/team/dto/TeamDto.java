package com.logmate.team.dto;

import com.logmate.team.model.Team;
import lombok.Getter;

@Getter
public class TeamDto {
    private long id;
    private String name;
    private String description;
    private Long teamFolderId; //팀 생성시 팀폴더 기본 생성
    private String myRole;

    public TeamDto(Team team, Long teamFolderId, String myRole) {
        this.id = team.getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.teamFolderId = teamFolderId;
        this.myRole = myRole;
    }
}
