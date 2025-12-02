package kr.co.wground.like.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.like.application.LikeService
import kr.co.wground.like.presentation.request.LikeRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/likes")
class LikeController(
    private val likeService: LikeService,
) {

    @PostMapping
    fun changeLike(
        @RequestBody request: LikeRequest,
        user: CurrentUserId,
    ): ResponseEntity<Unit> {
        likeService.changeLike(request.toDto(user.value))
        return ResponseEntity.noContent().build()
    }
}
