package kr.co.wground.comment.presentation

import jakarta.validation.Valid
import kr.co.wground.comment.application.CommentService
import kr.co.wground.comment.presentation.request.CommentCreateRequest
import kr.co.wground.comment.presentation.request.CommentUpdateRequest
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        commentService.write(commentDto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable commentId: CommentId,
        @Valid @RequestBody request: CommentUpdateRequest,
        writerId: CurrentUserId
    ): ResponseEntity<Void> {
        val commentDto = request.toDto(commentId)
        commentService.update(commentDto, writerId)
        return ResponseEntity.ok().build()
    }
}
