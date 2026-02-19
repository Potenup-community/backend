package kr.co.wground.post.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.application.dto.PostDetailDto.PostReactionDetailDto
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.LocalDateTime
import kr.co.wground.shop.application.dto.EquippedItem
import kr.co.wground.shop.application.dto.EquippedItemWithUserDto

data class PostDetailDto(
    val postId: PostId,
    val writerId: WriterId,
    val writerName: String,
    val title: String,
    val content: String,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
    val wroteAt: LocalDateTime,
    val trackName: String,
    val profileImageUrl: String,
    val nextPostId: PostId?,
    val nextPostTitle: String?,
    val previousPostId: PostId?,
    val previousPostTitle: String?,
    val reactions: List<PostReactionDetailDto> = emptyList(),
    val equippedItems:  List<EquippedItem>
) {
    data class PostReactionDetailDto(
        val reactionType: ReactionType,
        val count: Int,
    )
}

fun Post.toDto(
    writerName: String,
    commentsCount: Int,
    reactions: List<PostReaction>,
    trackName: String,
    profileImageUrl: String,
    nextPostId: PostId?,
    nextPostTitle: String?,
    previousPostId: PostId?,
    previousPostTitle: String?,
    equippedItemsWithUser: List<EquippedItemWithUserDto>
): PostDetailDto {
    val equippedItems = equippedItemsWithUser.map { EquippedItem.from(it) }
    return PostDetailDto(
        postId = id,
        writerId = writerId,
        writerName = writerName,
        title = postBody.title,
        content = postBody.content,
        topic = topic,
        highlightType = postStatus.highlightType,
        commentsCount = commentsCount,
        wroteAt = createdAt,
        trackName = trackName,
        nextPostId = nextPostId,
        previousPostId = previousPostId,
        nextPostTitle = nextPostTitle,
        previousPostTitle = previousPostTitle,
        profileImageUrl = profileImageUrl,
        reactions = reactions.groupingBy { it.reactionType }.eachCount()
            .map { (type, count) ->
                PostReactionDetailDto(
                    reactionType = type,
                    count = count
                )
            },
        equippedItems = equippedItems
    )
}
