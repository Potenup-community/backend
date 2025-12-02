package kr.co.wground.post.presentation

import jakarta.validation.Valid
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.global.common.WriterId
import kr.co.wground.global.config.resolver.CurrentUserId
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
    fun writePost(@Valid@RequestBody request: PostCreateRequest, writer: CurrentUserId): Long {
        return postService.createPost(request.toDto(writer.value))
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: PostId, writer: CurrentUserId) {
        postService.deletePost(id, writer.value)
    }

    @PatchMapping("/{id}")
    fun updatePost(
        @PathVariable id: PostId,
        @Valid@RequestBody request: PostUpdateRequest,
        writer: CurrentUserId
    ) {
        postService.updatePost(request.toDto(id, writer.value))
    }
}
