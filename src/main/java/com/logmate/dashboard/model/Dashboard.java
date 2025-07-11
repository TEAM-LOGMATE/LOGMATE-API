package com.logmate.dashboard.model;

import com.logmate.team.model.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard {
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
