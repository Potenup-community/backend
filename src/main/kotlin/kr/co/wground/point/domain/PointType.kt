enum class PointType(
    val description: String,
    val amount: Long,
    val dailyLimit: Int? = null
) {
    // [기본 활동]
    ATTENDANCE("출석 체크", 10, 1),
    ATTENDANCE_STREAK("연속 출석 보너스", 50, 1),

    // [콘텐츠 생성]
    WRITE_POST("게시글 작성", 50, 3),
    WRITE_COMMENT("댓글 작성", 10, 20),

    // [소셜 인터랙션]
    RECEIVE_LIKE_POST("게시글 좋아요 받음", 10, 5),
    RECEIVE_LIKE_COMMENT("댓글 좋아요 받음", 5, 5),

    // [소셜 인터랙션 - 주기]
    GIVE_LIKE_POST("게시글 좋아요 누름", 2, 5),
    GIVE_LIKE_COMMENT("댓글 좋아요 누름", 1, 5),

    // [스터디 & 결제]
    STUDY_CREATE("스터디 생성 (결제)", 500, null),
    STUDY_JOIN("스터디 참여 (결제)", 300, null),

    // [관리자]
    EVENT_ADMIN("이벤트 지급", 0, null),

    // [사용]
    USE_SHOP("상점 아이템 구매", 0, null);

    fun isLikeReward(): Boolean = this == RECEIVE_LIKE_POST || this == RECEIVE_LIKE_COMMENT
}