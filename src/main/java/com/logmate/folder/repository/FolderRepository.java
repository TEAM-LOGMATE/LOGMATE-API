package com.logmate.folder.repository;

import com.logmate.folder.model.Folder;
import com.logmate.global.BaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByTeamIdAndStatus(Long teamId, BaseStatus status);
    List<Folder> findByUserIdAndStatus(Long userId, BaseStatus status);
}
