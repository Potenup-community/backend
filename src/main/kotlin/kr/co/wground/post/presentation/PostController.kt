package kr.co.wground.post.presentation

import jakarta.validation.Valid
import java.net.URI
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.post.application.PostService
import kr.co.wground.post.presentation.request.PostCreateRequest
import kr.co.wground.post.presentation.request.PostUpdateRequest
import org.springframework.http.ResponseEntity
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
    fun writePost(@Valid @RequestBody request: PostCreateRequest, writer: CurrentUserId): ResponseEntity<Unit> {
        val createPost = postService.createPost(request.toDto(writer.value))
        val location = "/api/v1/posts/${createPost}"
        return ResponseEntity.created(URI.create(location)).build()
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: PostId, writer: CurrentUserId): ResponseEntity<Unit> {
        postService.deletePost(id, writer.value)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}")
    fun updatePost(
        @PathVariable id: PostId,
        @Valid @RequestBody request: PostUpdateRequest,
        writer: CurrentUserId
    ): ResponseEntity<Unit> {
        postService.updatePost(request.toDto(id, writer.value))
        return ResponseEntity.noContent().build()
    }
}
