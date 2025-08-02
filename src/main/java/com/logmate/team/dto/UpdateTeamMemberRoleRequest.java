package com.logmate.team.dto;

import lombok.Getter;

@Getter
public class UpdateTeamMemberRoleRequest {
    private Long userId;
    private String role; // "MEMBER", "MANAGER", "OWNER"
}
