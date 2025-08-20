package com.logmate.team.service;

import com.logmate.team.dto.CreateTeamRequest;
import com.logmate.team.dto.TeamDto;
import com.logmate.team.dto.UpdateTeamMemberRoleRequest;
import com.logmate.team.dto.UpdateTeamRequest;
import com.logmate.team.model.MemberRole;
import com.logmate.team.model.Team;
import com.logmate.team.model.TeamMember;
import com.logmate.team.repository.TeamMemberRepository;
import com.logmate.team.repository.TeamRepository;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public List<TeamDto> getTeamsByUser(User user) {
        List<TeamMember> memberships = teamMemberRepository.findByUser(user);
        return memberships.stream()
                .map(TeamMember::getTeam)
                .map(TeamDto::new)
                .collect(Collectors.toList());
    }

    public TeamDto createTeam(CreateTeamRequest request, User creator) {
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        List<TeamMember> members = new ArrayList<>();
        members.add(new TeamMember(team, creator, MemberRole.ADMIN));

        if (request.getMembers() != null) {
            for (var memberReq : request.getMembers()) {
                if (!memberReq.getUserId().equals(creator.getId())) {
                    User user = userRepository.findById(memberReq.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

                    MemberRole role = (memberReq.getRole() == null) ? MemberRole.MEMBER : memberReq.getRole();
                    members.add(new TeamMember(team, user, role));
                }
            }
        }

        team.setMembers(members);
        return new TeamDto(teamRepository.save(team));
    }
    // TODO 관리자가 아닌 유저 (팀 멤버) 를 팀에 추가하는 로직 구현하기

    public String generateInviteUrl(Long teamId) {
        String code = UUID.randomUUID().toString();
        return "https://logmate.com/invite/" + code;
    }

    public TeamDto updateTeam(Long teamId, UpdateTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀 없음"));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        Team updated = teamRepository.save(team);
        return new TeamDto(updated);
    }
    public void updateTeamMemberRole(Long teamId, UpdateTeamMemberRoleRequest request) {
        TeamMember member = teamMemberRepository.findByUserIdAndTeamId(request.getUserId(), teamId)
                .orElseThrow(() -> new RuntimeException("팀 멤버 없음"));

        member.setRole(request.getRole());
        teamMemberRepository.save(member);
    }

}
