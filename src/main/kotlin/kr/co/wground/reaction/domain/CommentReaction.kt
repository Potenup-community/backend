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
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.Instant

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "comment_reaction_uk",
            columnNames = ["user_id", "comment_id", "reaction_type"]
        )
    ],
    indexes = [
        Index(
            name = "idx_comment_id",
            columnList = "comment_id"
        )
    ]
)
class CommentReaction private constructor(
    @Column(updatable = false)
    val userId: UserId,

    @Column(updatable = false)
    val commentId: CommentId,

    @Column(updatable = false)
    val reactionType: ReactionType
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(updatable = false)
    lateinit var createdAt: Instant
        protected set

    @PrePersist
    fun onCreate() {
        createdAt = Instant.now()
    }

    // static factory --------------------

    companion object {
        fun create(userId: Long, commentId: Long, reactionType: ReactionType): CommentReaction {
            // To Do: 불변식 검증 해야 함
            return CommentReaction(userId = userId, commentId = commentId, reactionType = reactionType)
        }
    }
}
