package kr.co.wground.like.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
@Table(
    name = "likes",
    uniqueConstraints = [
        UniqueConstraint(
            name = "like_uk",
            columnNames = ["user_id", "post_id"]
        )
    ]
)
class Like(
    @Column(updatable = false)
    val userId: UserId,

    @Column(updatable = false)
    val postId: PostId,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
}
