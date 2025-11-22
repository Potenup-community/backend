package kr.co.wground.like.presentation

import kr.co.wground.like.application.LikeService
import kr.co.wground.like.presentation.request.LikeCreateRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/likes")
class LikeController(
    private val likeService: LikeService
) {

    @PostMapping
    fun likePost(@RequestBody request: LikeCreateRequest): ResponseEntity<Unit> {
        // TODO: Get userId from security context
        val userId = 1L
        likeService.likePost(request.toDto(userId))
        return ResponseEntity.noContent().build()
    }
}
