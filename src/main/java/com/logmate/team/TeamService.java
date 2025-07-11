package com.logmate.team;

import com.logmate.team.dto.TeamDto;
import com.logmate.team.repository.TeamMemberRepository;
import com.logmate.team.repository.TeamRepository;
import com.logmate.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    public List<TeamDto> getTeamsByUser(User user) {
        List<TeamMember> memberships = teamMemberRepository.findByUser(user);
        return memberships.stream()
                .map(TeamMember::getTeam)
                .map(TeamDto::new)
                .collect(Collectors.toList());
    }

    public TeamDto createTeam(String name, User user) {
        Team team = Team.builder()
                .name(name)
                .build();

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role("ADMIN")
                .build();

        team.getMembers().add(member);

        Team saved = teamRepository.save(team);
        return new TeamDto(saved);
    }
    // TODO 관리자가 아닌 유저 (팀 멤버) 를 팀에 추가하는 로직 구현하기
}
