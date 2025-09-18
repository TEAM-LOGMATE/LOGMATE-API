package com.logmate.dashboard.repository;

import com.logmate.dashboard.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    List<Dashboard> findByFolderId(Long folderId);
}
