package kr.co.wground.like.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import lombok.AccessLevel
import lombok.NoArgsConstructor
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Like(
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: Long,

    @Column(name = "post_id", nullable = false, updatable = false)
    val postId: Long,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}
