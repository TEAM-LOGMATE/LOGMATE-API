package com.logmate.team.repository;

import com.logmate.team.model.TeamMember;
import com.logmate.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    // 유저가 속한 팀 리스트
    List<TeamMember> findByUser(User user);
}
