package kr.co.wground.api.user.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @Column(name = "modified_at", nullable = false)
    lateinit var modifiedAt: LocalDateTime
        protected set

    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        this.createdAt = now
        this.modifiedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        this.modifiedAt = LocalDateTime.now()
    }
}
