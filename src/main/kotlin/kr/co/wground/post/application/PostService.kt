package kr.co.wground.post.application

import kr.co.wground.post.application.dto.PostCreateDto
import kr.co.wground.post.infra.PostRepository
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
) {
    fun createPost(dto: PostCreateDto): Long {
        return postRepository.save(dto.toDomain()).id
    }

    fun deletePost(id: Long) {
        //TODO(user 자신의 포스트인지 검증 로직 필요)
        postRepository.deleteById(id)
    }
}
