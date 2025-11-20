package kr.co.wground.post.domain

import jakarta.persistence.CascadeType.MERGE
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.time.LocalDateTime

@Entity
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val writerId: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    val topic: Topic,

    @OneToOne(cascade = [PERSIST, MERGE, REMOVE], orphanRemoval = true)
    @JoinColumn(name = "post_status_id")
    val postStatus: PostStatus,
) {
    companion object {
        fun from(
            writerId: Long,
            topic: Topic,
            title: String,
            content: String,
            highlightType: HighlightType? = null
        ): Post {
            return Post(
                writerId = writerId,
                topic = topic,
                title = title,
                content = content,
                postStatus = PostStatus(highlightType = highlightType),
            )
        }
    }
}
