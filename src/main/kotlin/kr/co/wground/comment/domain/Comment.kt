package kr.co.wground.comment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: CommentId = 0,

    @Column(updatable = false)
    val writerId: UserId,

    @Column(updatable = false)
    val postId: PostId,

    val parentId: CommentId? = null,

    content: String,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    modifiedAt: LocalDateTime = LocalDateTime.now(),
    deletedAt: LocalDateTime? = null,
) {
    @Lob
    var content: String = content
        protected set

    var modifiedAt: LocalDateTime = modifiedAt
        protected set

    var deletedAt: LocalDateTime? = deletedAt
        protected set

    var isDeleted: Boolean = false
        protected set

    fun update(content: String?) {
        content?.let { this.content = it }
        this.modifiedAt = LocalDateTime.now()
    }
}
