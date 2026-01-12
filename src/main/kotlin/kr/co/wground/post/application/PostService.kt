package kr.co.wground.post.application

import java.util.*
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.common.SyncDraftImagesToPostEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.application.dto.PostCreateDto
import kr.co.wground.post.application.dto.PostDetailDto
import kr.co.wground.post.application.dto.PostSummaryDto
import kr.co.wground.post.application.dto.PostUpdateDto
import kr.co.wground.post.application.dto.toDto
import kr.co.wground.post.application.dto.toDtos
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.post.infra.predicate.GetPostSummaryPredicate
import kr.co.wground.reaction.infra.jpa.PostReactionJpaRepository
import kr.co.wground.user.infra.CustomUserRepository
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: CustomUserRepository,
    private val postReactionRepository: PostReactionJpaRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun createPost(dto: PostCreateDto): Long {
        val postId = postRepository.save(dto.toDomain()).id

        sendSyncImageEvent(
            postId = postId,
            ownerId = dto.writerId,
            draftId = dto.draftId,
            content = dto.content
        )

        return postId
    }

    fun deletePost(id: Long, writerId: WriterId) {
        val foundPost = findPostByIdOrThrow(id)

        validatePostOwner(foundPost, writerId)
        postRepository.deleteById(id)
    }

    fun updatePost(dto: PostUpdateDto) {
        val foundPost = findPostByIdOrThrow(dto.postId)
        validatePostOwner(foundPost, dto.writerId)

        foundPost.update(
            topic = dto.topic,
            title = dto.title,
            content = dto.content,
            type = dto.highlightType
        )

        dto.content?.let {
            sendSyncImageEvent(
                postId = foundPost.id,
                ownerId = dto.writerId,
                draftId = dto.draftId,
                content = dto.content
            )
        }
    }

    private fun sendSyncImageEvent(
        postId: PostId,
        ownerId: WriterId,
        draftId: UUID,
        content: String,
    ) {
        eventPublisher.publishEvent(
            SyncDraftImagesToPostEvent(
                postId = postId,
                ownerId = ownerId,
                draftId = draftId,
                markdown = content,
            )
        )
    }

    private fun findPostByIdOrThrow(id: PostId): Post {
        return postRepository.findByIdOrNull(id)
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_POST)
    }

    private fun validatePostOwner(post: Post, writerId: WriterId) {
        if (post.writerId != writerId) throw BusinessException(PostErrorCode.YOU_ARE_NOT_OWNER_THIS_POST)
    }

    fun getSummary(userId: UserId, pageable: Pageable, topic: Topic?): Slice<PostSummaryDto> {
        val predicate = GetPostSummaryPredicate(pageable, topic)

        val posts = postRepository.findAllByPredicate(predicate)

        val postIds = posts.map { it.id }.toSet()
        val writerIds = posts.map { it.writerId }.toSet()

        val writersById = userRepository.findUserDisplayInfos(writerIds.toList())
        val postReactionStats = postReactionRepository.fetchPostReactionStatsRows(postIds, userId)
        val commentsCountById = commentRepository.countByPostIds(postIds.toList())

        return posts.toDtos(writersById, commentsCountById, postReactionStats)
    }

    fun getPostDetail(id: PostId): PostDetailDto {
        val foundCourse = findPostByIdOrThrow(id)
        val writer = findUserDisplayInfoByIdOrThrow(foundCourse.writerId)

        val reactionsByPostId = postReactionRepository.findPostReactionsByPostId(id)

        val commentsCount = commentRepository.countByPostIds(listOf(id))
            .firstOrNull()?.count ?: 0

        return foundCourse.toDto(
            writerName = writer.name,
            trackName = writer.trackName,
            profileImageUrl = writer.profileImageUrl,
            commentsCount = commentsCount,
            reactions = reactionsByPostId
        )
    }

    private fun findUserDisplayInfoByIdOrThrow(id: WriterId): UserDisplayInfoDto {
        return userRepository.findUserDisplayInfos(listOf(id))[id]
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_WRITER)
    }
}
