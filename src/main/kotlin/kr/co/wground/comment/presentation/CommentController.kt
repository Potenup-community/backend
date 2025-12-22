package kr.co.wground.comment.presentation

import jakarta.validation.Valid
import java.net.URI
import kr.co.wground.comment.application.CommentService
import kr.co.wground.comment.presentation.request.CommentCreateRequest
import kr.co.wground.comment.presentation.request.CommentUpdateRequest
import kr.co.wground.comment.presentation.response.CommentSliceResponse
import kr.co.wground.comment.presentation.response.toResponse
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
        writerId: CurrentUserId,
    ): ResponseEntity<Unit> {
        val commentDto = request.toDto(writerId)
        val location = "/api/v1/comments/${commentService.write(commentDto)}"
        return ResponseEntity.created(URI.create(location)).build()
    }

    @PutMapping("/{id}")
    fun updateComment(
        @PathVariable id: CommentId,
        @Valid @RequestBody request: CommentUpdateRequest,
        writerId: CurrentUserId,
    ): ResponseEntity<Unit> {
        val commentDto = request.toDto(id)
        commentService.update(commentDto, writerId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun deleteComment(
        @PathVariable id: CommentId,
        writerId: CurrentUserId,
    ): ResponseEntity<Unit> {
        commentService.delete(id, writerId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{postId}")
    fun getComments(
        @PathVariable postId: PostId,
        writerId: CurrentUserId,
        @PageableDefault(size = 20, sort = ["createdAt", "id"])
        pageable: Pageable,
    ): CommentSliceResponse = commentService
        .getCommentsByPost(postId, pageable, writerId)
        .toResponse()

}
