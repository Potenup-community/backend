package kr.co.wground.post.application

import java.util.UUID
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.common.event.AnnouncementCreatedEvent
import kr.co.wground.common.event.PostCreatedEvent
import kr.co.wground.common.event.SyncDraftImagesToPostEvent
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
import kr.co.wground.shop.application.query.InventoryQueryPort
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
    private val eventPublisher: ApplicationEventPublisher,
    private val inventoryQueryPort: InventoryQueryPort
) {
    fun createPost(dto: PostCreateDto): Long {
        val postId = postRepository.save(dto.toDomain()).id

        sendSyncImageEvent(
            postId = postId,
            ownerId = dto.writerId,
            draftId = dto.draftId,
            content = dto.content
        )

        noticeEventPublish(dto, postId)
        publishPostCreatedEvent(postId, dto.writerId)
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

    private fun noticeEventPublish(dto: PostCreateDto, postId: PostId) {
        if (dto.topic == Topic.NOTICE) {
            eventPublisher.publishEvent(
                AnnouncementCreatedEvent(
                    postId = postId,
                    title = dto.title,
                )
            )
        }
    }

    private fun publishPostCreatedEvent(postId: PostId, writerId: WriterId) {
        eventPublisher.publishEvent(PostCreatedEvent(postId, writerId))
    }

    private fun findPostByIdOrThrow(id: PostId): Post {
        return postRepository.findByIdOrNull(id)
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_POST)
    }

    private fun validatePostOwner(post: Post, writerId: WriterId) {
        if (post.writerId != writerId) throw BusinessException(PostErrorCode.YOU_ARE_NOT_OWNER_THIS_POST)
    }

    fun getSummary(userId: UserId, pageable: Pageable, topic: Topic?): Slice<PostSummaryDto> {
        val predicate = GetPostSummaryPredicate(pageable = pageable, topic = topic)

        val posts = postRepository.findAllByPredicate(predicate)

        return assembleSummaryDtos(posts, userId)
    }

    fun getPostDetail(id: PostId): PostDetailDto {
        val findPost = findPostByIdOrThrow(id)
        val writer = findUserDisplayInfoByIdOrThrow(findPost.writerId)
        val equippedItemsWithUser = inventoryQueryPort.getEquipItems(listOf(writer.userId))
        val reactionsByPostId = postReactionRepository.findPostReactionsByPostId(id)

        val commentsCount = commentRepository.countByPostIds(listOf(id))
            .firstOrNull()?.count ?: 0

        val postNavigationDto = postRepository.findIdsOfPreviousAndNext(
            findPost.id,
            findPost.createdAt
        )

        return findPost.toDto(
            writerName = writer.name,
            trackName = writer.trackName,
            profileImageUrl = writer.profileImageUrl,
            commentsCount = commentsCount,
            nextPostId = postNavigationDto.nextPostId,
            previousPostId = postNavigationDto.previousPostId,
            reactions = reactionsByPostId,
            nextPostTitle = postNavigationDto.nextPostTitle,
            previousPostTitle = postNavigationDto.previousPostTitle,
            equippedItemsWithUser = equippedItemsWithUser
        )
    }

    fun getMyPosts(userId: UserId, pageable: Pageable): Slice<PostSummaryDto> {
        val predicate = GetPostSummaryPredicate(pageable = pageable, userId = userId)
        val posts = postRepository.findAllByPredicate(predicate)

        return assembleSummaryDtos(posts, userId)
    }

    fun getMyLikedPosts(userId: UserId, pageable: Pageable): Slice<PostSummaryDto> {
        val posts = postReactionRepository.findAllLikedByUser(userId, pageable)

        return assembleSummaryDtos(posts, userId)
    }

    private fun assembleSummaryDtos(
        posts: Slice<Post>,
        userId: UserId,
    ): Slice<PostSummaryDto> {
        val postIds = posts.map { it.id }.toSet()
        val writerIds = posts.map { it.writerId }.toSet()
        val writerList = writerIds.toList()

        val writersById = userRepository.findUserDisplayInfos(writerList)
        val postReactionStats = postReactionRepository.fetchPostReactionStatsRows(postIds, userId)
        val commentsCountById = commentRepository.countByPostIds(postIds.toList())

        val equippedItems = inventoryQueryPort.getEquipItems(writerList)

        return posts.toDtos(writersById, commentsCountById, postReactionStats, equippedItems)
    }

    private fun findUserDisplayInfoByIdOrThrow(id: WriterId): UserDisplayInfoDto {
        return userRepository.findUserDisplayInfos(listOf(id))[id]
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_WRITER)
    }
}
