package com.logmate.dashboard.repository;

import com.logmate.dashboard.model.Dashboard;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DashboardRepository extends CrudRepository<Dashboard, Long> {
    List<Dashboard> findByTeamId(Long teamId);
}
