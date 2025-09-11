package com.logmate.folder.controller;

import com.logmate.folder.dto.FolderDto;
import com.logmate.folder.dto.FolderRequest;
import com.logmate.folder.service.FolderService;
import com.logmate.global.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderService folderService;

    // 팀 폴더 전체 조회
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<BaseResponse<List<FolderDto>>> getTeamFolders(@PathVariable Long teamId) {
        List<FolderDto> folders = folderService.getTeamFolders(teamId);
        return ResponseEntity.ok(BaseResponse.of(200, "팀 폴더 목록 조회 성공", folders));
    }

    // 팀 폴더 생성
   /* @PostMapping("/teams/{teamId}")
    public ResponseEntity<BaseResponse<FolderDto>> createTeamFolder(
            @PathVariable Long teamId,
            @RequestBody FolderRequest request) {
        FolderDto folder = folderService.createTeamFolder(teamId, request.getName());
        return ResponseEntity.ok(BaseResponse.of(200, "팀 폴더 생성 성공", folder));
    }
*/

    // 개인 폴더 전체 조회
    @GetMapping("/personal/{userId}")
    public ResponseEntity<BaseResponse<List<FolderDto>>> getPersonalFolders(@PathVariable Long userId) {
        List<FolderDto> folders = folderService.getPersonalFolders(userId);
        return ResponseEntity.ok(BaseResponse.of(200, "사용자 폴더 목록 조회 성공", folders));
    }

    // 개인 폴더 생성
    @PostMapping("/personal/{userId}")
    public ResponseEntity<BaseResponse<FolderDto>> createPersonalFolder(
            @PathVariable Long userId,
            @RequestBody FolderRequest request) {
        FolderDto folder = folderService.createPersonalFolder(userId, request.getName());
        return ResponseEntity.ok(BaseResponse.of(200, "사용자 폴더 생성 성공", folder));
    }

    // 폴더 수정
    @PutMapping("/{folderId}")
    public ResponseEntity<BaseResponse<FolderDto>> updateFolder(
            @PathVariable Long folderId,
            @RequestBody FolderRequest request) {
        FolderDto folder = folderService.updateFolder(folderId, request.getName());
        return ResponseEntity.ok(BaseResponse.of(200, "폴더 수정 성공", folder));
    }

    // 폴더 삭제 (soft delete)
    @DeleteMapping("/{folderId}")
    public ResponseEntity<BaseResponse<Void>> deleteFolder(
            @PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.ok(BaseResponse.of(200, "폴더 삭제 성공", null));
    }
}
