package kr.co.wground.point.domain

enum class ReferenceType(val description: String) {
    POST("게시글"),
    COMMENT("댓글"),
    POST_REACTION("게시글 리액션"),
    COMMENT_REACTION("댓글 리액션"),
    STUDY("스터디"),
    ATTENDANCE("출석"),
    EVENT("이벤트"),
    SHOP_ITEM("상점 아이템"),
}