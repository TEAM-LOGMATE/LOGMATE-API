package com.logmate.team.dto;

import com.logmate.team.Team;
import lombok.Getter;

@Getter
public class TeamDto {
    private long id;
    private String name;

    public TeamDto(Team team) {
        this.id = team.getId();
        this.name = team.getName();
    }
}
