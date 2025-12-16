package kr.co.wground.comment.presentation

import jakarta.validation.Valid
import java.net.URI
import kr.co.wground.comment.application.CommentService
import kr.co.wground.comment.presentation.request.CommentCreateRequest
import kr.co.wground.comment.presentation.request.CommentUpdateRequest
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        val location = "/api/v1/comments/${commentService.write(commentDto)}"
        return ResponseEntity.created(URI.create(location)).build()
    }

    @PutMapping("/{id}")
    fun updateComment(
        @PathVariable id: CommentId,
        @Valid @RequestBody request: CommentUpdateRequest,
        writerId: CurrentUserId
    ): ResponseEntity<Void> {
        val commentDto = request.toDto(id)
        commentService.update(commentDto, writerId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun deleteComment(
        @PathVariable id: CommentId,
        writerId: CurrentUserId,
    ): ResponseEntity<Void> {
        commentService.delete(id, writerId)
        return ResponseEntity.noContent().build()
    }
}
