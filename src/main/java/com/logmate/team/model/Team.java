package com.logmate.team.model;

import com.logmate.dashboard.model.Dashboard;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 팀 <-> 멤버 : teamMember 통해 연결
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL,  orphanRemoval = true)
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();
    // 팀 <-> 대시보드 : 1:N
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Dashboard> dashboards = new ArrayList<>();
}
