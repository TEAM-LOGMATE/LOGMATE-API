package com.logmate.dashboard.model;

import com.logmate.folder.model.Folder;
import com.logmate.global.BaseEntity;
import com.logmate.team.model.Team;
import com.logmate.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String logPath;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;   // 개인 대시보드용

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;  // 폴더 포함
}
