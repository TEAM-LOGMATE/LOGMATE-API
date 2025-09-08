package com.logmate.folder;

import com.logmate.dashboard.model.Dashboard;
import com.logmate.global.BaseEntity;
import com.logmate.team.model.Team;
import com.logmate.user.model.User;
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
public class Folder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; //팀 폴더

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; //개인 폴더

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dashboard> dashboards = new ArrayList<>();
}
