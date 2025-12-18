package kr.co.wground.reaction.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.reaction.application.ReactionService
import kr.co.wground.reaction.presentation.request.ReactionRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reactions")
class ReactionController(
    private val reactionService: ReactionService,
) {

    @PostMapping
    fun changeReaction(
        @RequestBody request: ReactionRequest,
        user: CurrentUserId,
    ): ResponseEntity<Unit> {
        reactionService.changeReaction(request.toDto(user.value))
        return ResponseEntity.noContent().build()
    }
}
