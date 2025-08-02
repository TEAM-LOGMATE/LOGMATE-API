package com.logmate.team.controller;


import com.logmate.team.dto.UpdateTeamMemberRoleRequest;
import com.logmate.team.dto.UpdateTeamRequest;
import com.logmate.team.service.TeamService;
import com.logmate.team.dto.CreateTeamRequest;
import com.logmate.team.dto.TeamDto;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final UserRepository userRepository;

    @GetMapping
    private ResponseEntity<List<TeamDto>> getMyTeams(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        List<TeamDto> myTeams = teamService.getTeamsByUser(user);
        return ResponseEntity.ok(myTeams);
    }

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@RequestBody CreateTeamRequest request,
                                              HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        TeamDto created = teamService.createTeam(request, user);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamDto> updateTeam(@PathVariable Long teamId,
                                              @RequestBody UpdateTeamRequest request) {
        TeamDto updated = teamService.updateTeam(teamId, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{teamId}/members/role")
    public ResponseEntity<Void> updateMemberRole(@PathVariable Long teamId,
                                                 @RequestBody UpdateTeamMemberRoleRequest request) {
        teamService.updateTeamMemberRole(teamId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{teamId}/invite")
    public ResponseEntity<String> getInviteUrl(@PathVariable Long teamId) {
        String url = teamService.generateInviteUrl(teamId);
        return ResponseEntity.ok(url);
    }
}
