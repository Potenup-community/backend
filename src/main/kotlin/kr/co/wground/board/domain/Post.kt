package kr.co.wground.board.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class Post(
    @Id
    var id: Long? = null,
    val writer: Long,
    val topic: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
) {
}