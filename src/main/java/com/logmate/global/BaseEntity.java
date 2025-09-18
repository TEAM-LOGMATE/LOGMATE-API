package com.logmate.global;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CHAR(1)", nullable = false)
    private BaseStatus status = BaseStatus.Y;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.updatedAt = this.createdAt; // 수정 없이 첫 생성인 경우 -> 생성일이 수정일이 됨
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정 시 갱신
    }
}
