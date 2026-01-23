package kr.co.wground.post.infra.dto

data class PostNavigationDto(
    val previousPostId: Long?,
    val nextPostId: Long?,
) {

}
