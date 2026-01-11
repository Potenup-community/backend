package kr.co.wground.reaction.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.reaction.docs.SwaggerReactionResponseExample
import kr.co.wground.reaction.domain.enums.ReactionType

@Schema(description = "게시글 리액션 조회 응답 객체", example = SwaggerReactionResponseExample.POST_REACTION_STATS_RESPONSE)
data class PostReactionStats(
    @field:Schema(
        name = "게시글 ID",
        example = "1"
    )
    val postId: PostId,
    @field:Schema(
        name = "해당 게시글의 전체 리액션 수",
        example = "13"
    )
    val totalCount: Int,
    @field:Schema(
        description = "반응 타입별 요약 (key = ReactionType, value = ReactionSummary)",
        type = "object",
        additionalProperties = Schema.AdditionalPropertiesValue.USE_ADDITIONAL_PROPERTIES_ANNOTATION,
        additionalPropertiesSchema = ReactionSummary::class,
        example = """
        {
          "LIKE":  { "count": 3, "reactedByMe": true },
          "HEART": { "count": 1, "reactedByMe": false },
          "SMILE": { "count": 0, "reactedByMe": false }
        }
        """
    )
    val summaries: Map<ReactionType, ReactionSummary>
) {
    companion object {
        fun emptyOf(postId: PostId) : PostReactionStats {
            return PostReactionStats(
                postId = postId,
                totalCount = 0,
                summaries = emptyMap()
            )
        }
    }
}