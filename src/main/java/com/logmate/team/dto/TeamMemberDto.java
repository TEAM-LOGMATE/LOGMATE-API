package com.logmate.team.dto;

import com.logmate.team.model.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamMemberDto {
    private String name;
    private String email;
    private MemberRole role;
}