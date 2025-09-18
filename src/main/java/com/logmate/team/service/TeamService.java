package com.logmate.team.service;

import com.logmate.folder.model.Folder;
import com.logmate.folder.repository.FolderRepository;
import com.logmate.folder.service.FolderService;
import com.logmate.global.BaseStatus;
import com.logmate.global.CustomException;
import com.logmate.team.dto.*;
import com.logmate.team.model.MemberRole;
import com.logmate.team.model.Team;
import com.logmate.team.model.TeamMember;
import com.logmate.team.repository.TeamMemberRepository;
import com.logmate.team.repository.TeamRepository;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final FolderService folderService;
    private final FolderRepository folderRepository;

    public List<TeamDto> getTeamsByUser(User user) {
        List<TeamMember> memberships = teamMemberRepository.findByUser(user);
        return memberships.stream()
                //.map(TeamMember::getTeam)
                .filter(team -> team.getTeam().getStatus() == BaseStatus.Y)
                .map(team -> {
                    Long folderId = folderRepository.findByTeamIdAndStatus(team.getTeam().getId(), BaseStatus.Y)
                            .stream()
                            .findFirst()
                            .map(Folder::getId)
                            .orElse(null);
                    return new TeamDto(team.getTeam(), folderId, team.getRole().name());
                })
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
                User user = null;
                if(memberReq.getEmail() != null){
                    user = userRepository.findByEmail(memberReq.getEmail())
                            .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 이메일 사용자를 찾을 수 없습니다."));
                } else if (memberReq.getUserId() != null) {
                    user = userRepository.findById(memberReq.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
                }
                if (user != null && !user.getId().equals(creator.getId())) {
                    MemberRole role = (memberReq.getRole() == null) ? MemberRole.MEMBER : memberReq.getRole();
                    members.add(new TeamMember(team, user, role));
                }
            }
        }

        team.setMembers(members);

        Team savedTeam = teamRepository.save(team); // 팀 저장

        Long teamFolderId = folderService.createTeamFolder(savedTeam.getId(), savedTeam.getName()).getId(); // 자동으로 팀 폴더 생성
        return new TeamDto(savedTeam, teamFolderId, MemberRole.ADMIN.name());
    }
    public String generateInviteUrl(Long teamId) {
        String code = UUID.randomUUID().toString();
        return "https://logmate.com/invite/" + code;
    }

    @Transactional
    public TeamDto updateTeam(Long teamId, UpdateTeamRequest request, User requester) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));

        if (request.getName() != null) team.setName(request.getName());

        Folder teamFolder = folderRepository.findByTeamIdAndStatus(teamId, BaseStatus.Y)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팀 폴더가 존재하지 않습니다."));
        teamFolder.setName(request.getName());
        folderRepository.save(teamFolder);
        if (request.getDescription() != null) team.setDescription(request.getDescription());

        //요청자 권한 확인
        TeamMember requesterMember = teamMemberRepository.findByUserIdAndTeamId(requester.getId(), teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "팀 멤버가 아닙니다."));

        boolean isAdmin = requesterMember.getRole() == MemberRole.ADMIN;
        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 팀을 수정할 수 있습니다.");
        }

        List<String> requestEmails = request.getMembers().stream()
                .map(UpdateTeamMemberRoleRequest::getEmail)
                .toList();

        team.getMembers().removeIf(m -> !requestEmails.contains(m.getUser().getEmail()));

        for (var memberReq : request.getMembers()) {
            User user = userRepository.findByEmail(memberReq.getEmail())
                    .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "해당 이메일 사용자를 찾을 수 없습니다."));

            TeamMember member = teamMemberRepository.findByUserIdAndTeamId(user.getId(), teamId).orElse(null);

            if (member == null) {
                // 신규 멤버 추가
                MemberRole role = (memberReq.getRole() == null) ? MemberRole.MEMBER : memberReq.getRole();
                team.getMembers().add(new TeamMember(team, user, role));
            } else {
                member.setRole(memberReq.getRole()); //역할 업데이트
            }
        }

        Team updated = teamRepository.save(team);
        return new TeamDto(updated, teamFolder.getId(), requesterMember.getRole().name());
    }

    @Transactional
    public void deleteTeam(Long teamId, User requester) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));

        // 요청자 권한 확인 (관리자만 팀 삭제 가능)
        TeamMember requesterMember = teamMemberRepository.findByUserIdAndTeamId(requester.getId(), teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "팀 멤버가 아닙니다."));

        if (requesterMember.getRole() != MemberRole.ADMIN) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 팀을 삭제할 수 있습니다.");
        }

        // 팀 soft delete
        team.setStatus(BaseStatus.N);
        teamRepository.save(team);

        // 팀 기본 폴더도 soft delete
        folderRepository.findByTeamIdAndStatus(teamId, BaseStatus.Y)
                .forEach(folder -> {
                    folder.setStatus(BaseStatus.N);
                    folderRepository.save(folder);
                });
        //팀멤버 일단은 하드딜리트 -> 추후 변경 가능
        team.getMembers().forEach(member -> {
            teamMemberRepository.delete(member);
        });
    }

    public TeamDetailDto getTeamDetail(Long teamId, User requester) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));

        Folder teamFolder = folderRepository.findByTeamIdAndStatus(teamId, BaseStatus.Y)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팀 폴더가 존재하지 않습니다."));

        List<TeamMember> members = teamMemberRepository.findByUserIdAndTeamId(requester.getId(), teamId)
                .map(m -> team.getMembers()) // 팀 멤버 전체 가져오기
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "팀 멤버가 아닙니다."));

        List<TeamMemberDto> memberDtos = members.stream()
                .map(m -> new TeamMemberDto(
                        m.getUser().getName(),
                        m.getUser().getEmail(),
                        m.getRole()
                ))
                .toList();

        return new TeamDetailDto(team, teamFolder.getId(), memberDtos);
    }

}
