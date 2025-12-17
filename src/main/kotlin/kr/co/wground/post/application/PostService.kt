package kr.co.wground.post.application

import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.application.dto.PostCreateDto
import kr.co.wground.post.application.dto.PostDetailDto
import kr.co.wground.post.application.dto.PostSummaryDto
import kr.co.wground.post.application.dto.PostUpdateDto
import kr.co.wground.post.application.dto.toDto
import kr.co.wground.post.application.dto.toDtos
import kr.co.wground.post.domain.Post
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
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

    fun getSummary(): List<PostSummaryDto> {
        //TODO(Comment 개수 조회 추가 예정 blocked by Comment)

        val posts = postRepository.findAll()
        val writers = userRepository.findAllById(posts.map { it.writerId }.toSet())

        return posts.toDtos(writers)
    }

    fun getCourse(id: PostId): PostDetailDto {
        //TODO(Comment 개수 조회 추가 예정 blocked by Comment)

        val foundCourse = findPostByIdOrThrow(id)
        val writer = findUserByIdOrThrow(foundCourse.writerId)

        return foundCourse.toDto(writer.name)
    }

    private fun findUserByIdOrThrow(id: WriterId): User {
        return userRepository.findByIdOrNull(id) ?:
            throw BusinessException(PostErrorCode.NOT_FOUND_WRITER)
    }
}
