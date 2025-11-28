package kr.co.wground.comment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.PreUpdate
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
class Comment private constructor(
    @Column(updatable = false)
    val writerId: UserId,

    @Column(updatable = false)
    val postId: PostId,

    @Column(updatable = false)
    val parentId: CommentId? = null,

    content: String,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    modifiedAt: LocalDateTime = LocalDateTime.now(),
    deletedAt: LocalDateTime? = null,
) {
    protected constructor() : this(writerId = 0, postId = 0, content = "", parentId = null)

    companion object {
        private const val MAX_CONTENT_LENGTH = 2000

        fun create(writerId: UserId, postId: PostId, parentId: CommentId?, content: String): Comment {
            validateContent(content)
            return Comment(writerId, postId, parentId, content)
        }

        private fun validateContent(content: String) {
            when {
                content.isBlank() ->
                    throw BusinessException(CommentErrorCode.CONTENT_IS_EMPTY)

                content.length > MAX_CONTENT_LENGTH ->
                    throw BusinessException(CommentErrorCode.CONTENT_IS_TOO_LONG)
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: CommentId = 0

    @Lob
    var content: String = content
        protected set

    var modifiedAt: LocalDateTime = modifiedAt
        protected set

    var deletedAt: LocalDateTime? = deletedAt
        protected set

    var isDeleted: Boolean = false
        protected set

    fun isParent(): Boolean {
        return parentId == null
    }

    @PreUpdate
    private fun onPreUpdate() {
        this.modifiedAt = LocalDateTime.now()
    }

    fun updateContent(newContent: String) {
        validateContent(newContent)
        this.content = newContent
    }
}
