package com.logmate.webhook.repository;

import com.logmate.webhook.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook,Long> {
    List<Webhook> findByUserIdAndActiveTrue(Long userId);
}
