package com.logmate.team.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CreateTeamRequest {
    private String name;
    private String description;
    private List<MemberRoleRequest> members;
}
