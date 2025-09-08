package com.logmate.folder;

import com.logmate.global.CustomException;
import com.logmate.team.model.Team;
import com.logmate.team.repository.TeamRepository;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public FolderDto createTeamFolder(Long teamId, String name) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

        Folder folder = Folder.builder()
                .name(name)
                .team(team)
                .build();

        return FolderDto.from(folderRepository.save(folder));
    }

    public FolderDto createUserFolder(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Folder folder = Folder.builder()
                .name(name)
                .user(user)
                .build();

        return FolderDto.from(folderRepository.save(folder));
    }

    public List<FolderDto> getTeamFolders(Long teamId) {
        return folderRepository.findByTeamId(teamId).stream()
                .map(FolderDto::from)
                .toList();
    }

    public List<FolderDto> getUserFolders(Long userId) {
        return folderRepository.findByUserId(userId).stream()
                .map(FolderDto::from)
                .toList();
    }
}
