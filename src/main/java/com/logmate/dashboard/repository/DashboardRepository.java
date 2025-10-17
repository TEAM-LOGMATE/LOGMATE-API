package com.logmate.dashboard.repository;

import com.logmate.dashboard.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    List<Dashboard> findByFolderId(Long folderId);
    @Query("select d.id from Dashboard d where d.folder.id = :folderId")
    List<Long> findIdsByFolderId(@Param("folderId") Long folderId);
}
