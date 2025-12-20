package kr.co.wground.reaction.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.reaction.application.ReactionCommandService
import kr.co.wground.reaction.presentation.request.ReactionRequest
import kr.co.wground.reaction.presentation.request.ReactionTarget.COMMENT
import kr.co.wground.reaction.presentation.request.ReactionTarget.POST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reactions")
class ReactionController(
    private val reactionCommandService: ReactionCommandService,
) {

    @PostMapping
    fun react(
        @RequestBody request: ReactionRequest,
        user: CurrentUserId,
    ): ResponseEntity<Unit> {

        val userId = user.value;

        when (request.targetType) {
            POST -> reactionCommandService.reactToPost(request.toPostReactCommand(userId))
            COMMENT -> reactionCommandService.reactToComment(request.toCommentReactCommand(userId))
        }

        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun unreact(
        @RequestBody request: ReactionRequest,
        user: CurrentUserId,
    ): ResponseEntity<Unit> {

        val userId = user.value;

        when (request.targetType) {
            POST -> reactionCommandService.unreactToPost(request.toPostReactCommand(userId))
            COMMENT -> reactionCommandService.unreactToComment(request.toCommentReactCommand(userId))
        }

        return ResponseEntity.noContent().build()
    }
}
