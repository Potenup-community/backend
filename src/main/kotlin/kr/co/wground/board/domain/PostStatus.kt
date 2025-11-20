package kr.co.wground.board.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class PostStatus(
    @Id
    val id: Long? = null,
    val viewCount: Int,
    val commentCount: Int,
    val likeCount: Int,
    @Enumerated(EnumType.STRING)
    val highlightType: HighlightType,
    val isDeleted: Boolean,

    @ManyToOne
    @JoinColumn(name = "post_id")
    val post: Post
) {
}