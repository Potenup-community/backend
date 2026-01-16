package kr.co.wground.comment.presentation

import jakarta.validation.Valid
import java.net.URI
import kr.co.wground.comment.application.CommentService
import kr.co.wground.comment.presentation.request.CommentCreateRequest
import kr.co.wground.comment.presentation.request.CommentUpdateRequest
import kr.co.wground.comment.presentation.response.CommentSummaryResponse
import kr.co.wground.comment.presentation.response.CommentsResponse
import kr.co.wground.comment.presentation.response.LikedCommentsResponse
import kr.co.wground.comment.presentation.response.MyCommentsResponse
import kr.co.wground.comment.presentation.response.toResponse
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
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
) : CommentApi {
    @PostMapping
    override fun writeComment(
        @Valid @RequestBody request: CommentCreateRequest,
        writerId: CurrentUserId,
    ): ResponseEntity<Unit> {
        val commentDto = request.toDto(writerId)
        val location = "/api/v1/comments/${commentService.write(commentDto)}"
        return ResponseEntity.created(URI.create(location)).build()
    }

    @PutMapping("/{id}")
    override fun updateComment(
        @PathVariable id: CommentId,
        @Valid @RequestBody request: CommentUpdateRequest,
        writerId: CurrentUserId,
    ): ResponseEntity<Unit> {
        val commentDto = request.toDto(id)
        commentService.update(commentDto, writerId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    override fun deleteComment(
        @PathVariable id: CommentId,
        writerId: CurrentUserId,
    ): ResponseEntity<Unit> {
        commentService.delete(id, writerId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{postId}")
    override fun getComments(
        @PathVariable postId: PostId,
        writerId: CurrentUserId
    ): ResponseEntity<CommentsResponse> {
        val result = commentService.getCommentsByPost(postId, writerId)

        return ResponseEntity.ok(
            CommentsResponse(result.map { CommentSummaryResponse.from(it) }
            )
        )
    }

    @GetMapping("/me")
    override fun getCommentsByMe(
        @PageableDefault(size = 20)
        @SortDefault(sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        userId: CurrentUserId
    ): ResponseEntity<MyCommentsResponse> {
        val result = commentService.getCommentsByMe(userId, pageable)

        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/me/liked")
    override fun getLikedComments(
        @PageableDefault(size = 20)
        @SortDefault(sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        userId: CurrentUserId,
    ): ResponseEntity<LikedCommentsResponse> {
        return ResponseEntity.ok(commentService.getLikedComments(userId, pageable).toResponse())
    }
}
