package com.logmate.team.dto;

import com.logmate.team.model.MemberRole;
import lombok.Getter;

@Getter
public class UpdateTeamMemberRoleRequest {
    private Long userId;
    private String email;
    private MemberRole role;
    private boolean remove;
}
