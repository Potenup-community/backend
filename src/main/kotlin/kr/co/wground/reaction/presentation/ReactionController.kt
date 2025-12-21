package kr.co.wground.reaction.presentation

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.reaction.application.ReactionCommandService
import kr.co.wground.reaction.application.ReactionQueryService
import kr.co.wground.reaction.presentation.request.ReactionRequest
import kr.co.wground.reaction.presentation.request.ReactionTarget.COMMENT
import kr.co.wground.reaction.presentation.request.ReactionTarget.POST
import kr.co.wground.reaction.application.dto.PostReactionStats
import kr.co.wground.reaction.presentation.request.PostReactionStatsBatchRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reactions")
class ReactionController(
    private val reactionCommandService: ReactionCommandService,
    private val reactionQueryService: ReactionQueryService,
) {

    // commands --------------------

    @PostMapping
    fun react(
        @Valid @RequestBody request: ReactionRequest,
        user: CurrentUserId,
    ): ResponseEntity<Unit> {

        val userId = user.value

        when (request.targetType) {
            POST -> reactionCommandService.reactToPost(request.toPostReactCommand(userId))
            COMMENT -> reactionCommandService.reactToComment(request.toCommentReactCommand(userId))
        }

        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun unreact(
        @Valid @RequestBody request: ReactionRequest,
        user: CurrentUserId,
    ): ResponseEntity<Unit> {

        val userId = user.value

        when (request.targetType) {
            POST -> reactionCommandService.unreactToPost(request.toPostReactCommand(userId))
            COMMENT -> reactionCommandService.unreactToComment(request.toCommentReactCommand(userId))
        }

        return ResponseEntity.noContent().build()
    }

    // queries --------------------

    @GetMapping("/posts/{postId}")
    fun getPostReactionStats(
        @NotNull(message = "postId 가 null 입니다.")
        @Positive(message = "postId 는 0 또는 음수일 수 없습니다.")
        @PathVariable("postId")
        postId: PostId,
        user: CurrentUserId
    ): ResponseEntity<PostReactionStats> {

        val userId = user.value

        // To Do: 서비스 쿼리 결과를 담는 dto 를 XxxResponse Dto 로 매핑하는 로직 추가
        val result = reactionQueryService.getPostReactionStats(userId, postId)

        return ResponseEntity.ok(result)
    }

    @GetMapping("/posts")
    fun getPostReactionStats(
        @Valid
        @RequestBody
        request: PostReactionStatsBatchRequest,
        user: CurrentUserId
    ): ResponseEntity<Map<PostId, PostReactionStats>> {

        val userId = user.value;

        val result = reactionQueryService.getPostReactionStats(request.postIds, userId)

        return ResponseEntity.ok(result)
    }
}
