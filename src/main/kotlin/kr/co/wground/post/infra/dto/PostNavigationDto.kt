package kr.co.wground.post.infra.dto

data class PostNavigationDto(
    val previousPostId: Long?,
    val previousPostTitle: String?,
    val nextPostId: Long?,
    val nextPostTitle: String?,
) {

}
