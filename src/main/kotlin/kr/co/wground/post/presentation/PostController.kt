package kr.co.wground.post.presentation

import jakarta.validation.Valid
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.application.PostService
import kr.co.wground.post.presentation.request.PostCreateRequest
import kr.co.wground.post.presentation.request.PostUpdateRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/posts")
class PostController(
    private val postService: PostService,
) {
    @PostMapping
    fun writePost(@Valid@RequestBody request: PostCreateRequest, writerId: UserId = 1): Long {
        return postService.createPost(request.toDto(writerId))
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: PostId) {
        postService.deletePost(id)
    }

    @PatchMapping("/{id}")
    fun updatePost(
        @PathVariable id: PostId,
        @Valid@RequestBody request: PostUpdateRequest,
        writerId: UserId = 1
    ) {
        postService.updatePost(request.toDto(id, writerId))
    }
}
