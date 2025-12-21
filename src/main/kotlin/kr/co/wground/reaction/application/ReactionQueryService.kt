package kr.co.wground.reaction.application

import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.exception.ReactionErrorCode
import kr.co.wground.reaction.infra.PostReactionJpaRepository
import kr.co.wground.reaction.application.dto.PostReactionStats
import kr.co.wground.reaction.application.dto.ReactionSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReactionQueryService(
    private val postReactionJpaRepository: PostReactionJpaRepository,
    private val postRepository: PostRepository,
) {

    fun getPostReactionStats(postId: PostId, userId: UserId): PostReactionStats {
        validatePostExistence(postId)

        val reactions = postReactionJpaRepository.findPostReactionsByPostId(postId)
        val groupedByType = reactions.groupBy { it.reactionType }
        val userReactionTypes = reactions
            .filter { it.userId == userId }
            .map { it.reactionType }
            .toSet()

        val summaries = groupedByType.mapValues { (reactionType, reactionList) ->
            ReactionSummary(
                count = reactionList.size,
                reactedByMe = reactionType in userReactionTypes
            )
        }

        return PostReactionStats(
            postId = postId,
            totalCount = reactions.size.toLong(),
            summaries = summaries
        )
    }

    // validation --------------------

    fun validatePostExistence(postId: PostId) {
        if (!postRepository.existsById(postId)) {
            throw BusinessException(ReactionErrorCode.POST_NOT_FOUND)
        }
    }
}