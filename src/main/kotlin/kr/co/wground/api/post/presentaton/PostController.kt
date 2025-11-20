package kr.co.wground.api.post.presentaton

import jakarta.validation.Valid
import kr.co.wground.api.post.application.PostService
import kr.co.wground.api.post.presentaton.request.PostCreateRequest
import org.springframework.web.bind.annotation.DeleteMapping
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
    fun writePost(@Valid@RequestBody request: PostCreateRequest, writerId: Long = 1): Long {
        return postService.createPost(request.toDto(writerId))
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: Long) {
        postService.deletePost(id);
    }
}