package kr.co.wground.post.domain

import jakarta.persistence.CascadeType.MERGE
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
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
    title: String,
    content: String,
    topic: Topic,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null,

    @OneToOne(cascade = [PERSIST, MERGE, REMOVE], orphanRemoval = true)
    @JoinColumn(name = "post_status_id")
    val postStatus: PostStatus,
) {
    @Enumerated(EnumType.STRING)
    var topic: Topic = topic
        protected set

    var title: String = title
        protected set

    @Lob
    var content: String = content
        protected set

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

    fun update(topic: Topic?, title: String?, content: String?) {
        topic?.let { this.topic = it}
        title?.let { this.title = it }
        content?.let { this.content = it }
    }
}
