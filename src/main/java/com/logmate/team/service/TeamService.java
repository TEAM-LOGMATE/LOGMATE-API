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
import jakarta.transaction.Transactional;
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
    public String generateInviteUrl(Long teamId) {
        String code = UUID.randomUUID().toString();
        return "https://logmate.com/invite/" + code;
    }

    @Transactional
    public TeamDto updateTeam(Long teamId, UpdateTeamRequest request, User requester) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        if (request.getName() != null) team.setName(request.getName());
        if (request.getDescription() != null) team.setDescription(request.getDescription());

        //요청자 권한 확인
        TeamMember requesterMember = teamMemberRepository.findByUserIdAndTeamId(requester.getId(), teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 멤버가 아님"));

        boolean isAdmin = requesterMember.getRole() == MemberRole.ADMIN;

        if (request.getMembers() != null) {
            for (var memberReq : request.getMembers()) {
                TeamMember member = teamMemberRepository.findByUserIdAndTeamId(memberReq.getUserId(), teamId)
                        .orElseThrow(() -> new IllegalArgumentException("팀 멤버 없음"));

                // 삭제 요청
                if (memberReq.isRemove()) {
                    boolean isSelf = member.getUser().getId().equals(requester.getId());

                    if(isSelf){
                        if(isAdmin){throw new IllegalArgumentException("관리자는 자기 자신을 삭제할 수 없습니다.");}
                        teamMemberRepository.delete(member);
                    }else {
                        if (!isAdmin) {throw new IllegalArgumentException("관리자만 다른 멤버를 삭제할 수 있습니다.");}
                        teamMemberRepository.delete(member);
                    }
                    continue;
                }

                // 역할 변경 요청
                if (memberReq.getRole() != null) {
                    if (!isAdmin) {throw new IllegalArgumentException("관리자만 역할을 변경할 수 있습니다.");}
                    member.setRole(memberReq.getRole());
                    teamMemberRepository.save(member);
                }
            }
        }

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
