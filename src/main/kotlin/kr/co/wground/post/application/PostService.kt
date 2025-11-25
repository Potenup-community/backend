package kr.co.wground.post.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.PostId
import kr.co.wground.post.application.dto.PostCreateDto
import kr.co.wground.post.application.dto.PostUpdateDto
import kr.co.wground.post.domain.Post
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.post.infra.PostRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
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

    fun updatePost(dto: PostUpdateDto) {
        //TODO(user 자신의 포스트인지 검증 로직 필요)
        val foundPost = findPostByIdOrThrow(dto.id)

        foundPost.update(dto.title, dto.content)
    }

    private fun findPostByIdOrThrow(id: PostId): Post {
        return postRepository.findByIdOrNull(id)
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_POST)
    }
}
