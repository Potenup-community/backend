package kr.co.wground.comment.presentation

import jakarta.validation.Valid
import kr.co.wground.comment.application.CommentService
import kr.co.wground.comment.presentation.request.CommentCreateRequest
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/comments")
class CommentController(
    private val commentService: CommentService,
) {
    @PostMapping
    fun writeComment(
        @Valid @RequestBody request: CommentCreateRequest,
        writerId: CurrentUserId
    ): ResponseEntity<Void> {
        val commentDto = request.toDto(writerId)
        val commentId = commentService.write(commentDto)
        return ResponseEntity.created(URI.create("/api/v1/comments$commentId")).build()
    }
}
