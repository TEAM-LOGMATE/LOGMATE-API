package com.logmate.dashboard.model;

import com.logmate.global.BaseEntity;
import com.logmate.team.model.Team;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
    private String sendUrl;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
