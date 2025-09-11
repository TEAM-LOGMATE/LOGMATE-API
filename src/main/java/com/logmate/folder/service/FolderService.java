package com.logmate.folder.service;

import com.logmate.folder.dto.FolderDto;
import com.logmate.folder.model.Folder;
import com.logmate.folder.repository.FolderRepository;
import com.logmate.global.BaseStatus;
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

    // 폴더 생성 (팀)
    public FolderDto createTeamFolder(Long teamId, String name) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

        Folder folder = Folder.builder()
                .name(name)
                .team(team)
                .build();

        return FolderDto.from(folderRepository.save(folder));
    }

    //폴더 생성 (개인)
    public FolderDto createPersonalFolder(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Folder folder = Folder.builder()
                .name(name)
                .user(user)
                .build();

        return FolderDto.from(folderRepository.save(folder));
    }


    public List<FolderDto> getTeamFolders(Long teamId) {
        return folderRepository.findByTeamIdAndStatus(teamId, BaseStatus.Y).stream()
                .map(FolderDto::from)
                .toList();
    }

    public List<FolderDto> getPersonalFolders(Long userId) {
        return folderRepository.findByUserIdAndStatus(userId, BaseStatus.Y).stream()
                .map(FolderDto::from)
                .toList();
    }

    // 폴더 수정
    public FolderDto updateFolder(Long folderId, String name) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다."));

        if (folder.getStatus() == BaseStatus.N) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "삭제된 폴더는 수정할 수 없습니다.");
        }

        if (folder.getTeam() != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "개인 폴더인 경우만 직접 수정 가능합니다. 팀 폴더 이름은 팀 수정 api를 통해 수정하세요.");
        }

        folder.setName(name);
        return FolderDto.from(folderRepository.save(folder));
    }

    // 폴더 삭제 (soft delete)
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다."));

        if (folder.getStatus() == BaseStatus.N) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 삭제된 폴더입니다.");
        }

        if (folder.getTeam() != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "팀 기본 폴더는 직접 삭제할 수 없습니다. 팀 삭제 시 함께 처리됩니다.");
        }

        folder.setStatus(BaseStatus.N);
        folderRepository.save(folder);
    }
}
