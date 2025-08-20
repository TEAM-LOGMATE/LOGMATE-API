package com.logmate.team.dto;

import com.logmate.team.model.MemberRole;
import lombok.Getter;

@Getter
public class MemberRoleRequest {
    private Long userId;
    private MemberRole role; // admin, member, viewer
}
