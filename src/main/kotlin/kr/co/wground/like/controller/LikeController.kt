package kr.co.wground.like.controller

import kr.co.wground.like.service.LikeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
class LikeController(
    private val likeService: LikeService
) {

    @PostMapping
    fun likePost(@PathVariable postId: Long): ResponseEntity<Unit> {
        // TODO: Get userId from security context
        val userId = 1L
        likeService.likePost(userId, postId)
        return ResponseEntity.noContent().build()
    }
}
