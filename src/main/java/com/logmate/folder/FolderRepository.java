package com.logmate.folder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByTeamId(Long teamId);
    List<Folder> findByUserId(Long userId);
}
