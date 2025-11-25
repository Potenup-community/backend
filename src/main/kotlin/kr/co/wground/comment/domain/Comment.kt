package kr.co.wground.comment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(updatable = false)
    val writerId: UserId,

    @Column(updatable = false)
    val postId: PostId,

    parentId: Long?,
    content: String,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null,
) {
    var parentId: Long? = parentId
        protected set

    @Lob
    var content: String = content
        protected set

    var isDeleted: Boolean = false
        protected set
}
