package kr.co.wground.post.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
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

    fun deletePost(id: Long, writerId: WriterId) {
        val foundPost = findPostByIdOrThrow(id)

        validatePostOwner(foundPost, writerId)
        postRepository.deleteById(id)
    }

    fun updatePost(dto: PostUpdateDto) {
        val foundPost = findPostByIdOrThrow(dto.id)
        validatePostOwner(foundPost, dto.writerId)

        foundPost.update(
            topic = dto.topic,
            title = dto.title,
            content = dto.content,
            type = dto.highlightType
        )
    }

    private fun findPostByIdOrThrow(id: PostId): Post {
        return postRepository.findByIdOrNull(id)
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_POST)
    }

    private fun validatePostOwner(post: Post, writerId: WriterId) {
        if (post.writerId != writerId) throw BusinessException(PostErrorCode.YOU_ARE_NOT_OWNER_THIS_POST)
    }
}
