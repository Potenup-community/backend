package kr.co.wground.comment.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.UserId

data class CommentAuthorResponse(
    @field:Schema(description = "댓글 작성자 ID")
    val userId: UserId,
    @field:Schema(description = "댓글 작성자 이름")
    val name: String,
    @field:Schema(description = "댓글 작성자 트랙 이름")
    val trackName: String,
    @field:Schema(description = "댓글 작성자 프로필 이미지 URL")
    val profileImageUrl: String,
)
