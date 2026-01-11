package kr.co.wground.reaction.application.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "리액션 횟수 및 사용자에 의해 리액션 되었는 지의 여부")
data class ReactionSummary(
    @field:Schema(
        description = "해당 타입 반응 수",
        example = "3",
    )
    val count: Int = 0,

    @field:Schema(
        description = "현재 로그인 사용자가 해당 타입으로 반응했는지 여부",
        example = "true"
    )
    val reactedByMe: Boolean
) {
}