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
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "post_reaction_uk",
            columnNames = ["user_id", "post_id", "reaction_type"]
        )
    ],
    indexes = [
        Index(
            name = "idx_post_id",
            columnList = "post_id"
        )
    ]
)
class PostReaction(
    @Column(updatable = false)
    val userId: UserId,

    @Column(updatable = false)
    val postId: PostId,

    @Column(updatable = false)
    val reactionType: ReactionType
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(updatable = false)
    lateinit var createdAt: Instant
        protected set;

    @PrePersist
    fun onCreate() {
        createdAt = Instant.now();
    }
}
