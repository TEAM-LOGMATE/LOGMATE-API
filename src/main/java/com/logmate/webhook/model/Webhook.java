package com.logmate.webhook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //팀 id or 사용자 id
    private Long userId;

    @Enumerated(EnumType.STRING)
    private WebhookType type; // SLACK, DISCORD, CUSTOM

    private String url;

    private boolean active = true;
}
