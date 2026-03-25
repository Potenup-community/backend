package kr.co.wground.reaction.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "project_reaction_uk",
            columnNames = ["user_id", "project_id", "reaction_type"]
        )
    ],
    indexes = [
        Index(
            name = "idx_project_reaction_project_id",
            columnList = "project_id"
        )
    ]
)
class ProjectReaction private constructor(
    @Column(updatable = false)
    val userId: UserId,

    @Column(updatable = false)
    val projectId: ProjectId,

    @Column(updatable = false)
    val reactionType: ReactionType,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
    }

    companion object {
        fun create(userId: UserId, projectId: ProjectId, reactionType: ReactionType) =
            ProjectReaction(userId = userId, projectId = projectId, reactionType = reactionType)
    }
}
