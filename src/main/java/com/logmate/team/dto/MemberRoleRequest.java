package com.logmate.team.dto;

import lombok.Getter;

@Getter
public class MemberRoleRequest {
    private Long userId;
    private String role; // admin, member, viewer
}
