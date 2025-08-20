package com.logmate.team.model;

import com.logmate.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Team <-> User : N:M
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)  // DB에 문자열로 저장
    private MemberRole role;

    public TeamMember(Team team, User user, MemberRole role) {
        this.team = team;
        this.user = user;
        this.role = role;
    }
}
