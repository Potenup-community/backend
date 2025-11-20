package kr.co.wground.api.post.application

import kr.co.wground.api.post.application.dto.PostCreateDto
import kr.co.wground.api.post.infra.PostRepository
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
) {
    fun createPost(dto: PostCreateDto): Long {
        return postRepository.save(dto.toDomain()).id
    }
}