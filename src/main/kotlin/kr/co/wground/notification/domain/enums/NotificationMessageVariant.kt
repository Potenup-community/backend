package kr.co.wground.notification.domain.enums

enum class NotificationMessageVariant(
    val type: NotificationType,
    val message: String
) {
    // ===== 공지사항 알림 =====
    ANNOUNCEMENT_AREUM(NotificationType.ANNOUNCEMENT, " 📢 중요 공지! 바로 확인해보세요~"),
    ANNOUNCEMENT_JAKYUNG(NotificationType.ANNOUNCEMENT, "여러분~!\n 공지 꼭 확인 부탁드립니다!"),
    ANNOUNCEMENT_KWANGMIN(NotificationType.ANNOUNCEMENT, "공지사항 전달드립니다~!"),
    ANNOUNCEMENT_EUNHAE(NotificationType.ANNOUNCEMENT, "📰 New News~"),

    // ===== 댓글 알림 =====
    POST_COMMENT_AREUM(NotificationType.POST_COMMENT, "내 글에 댓글이 달렸어요! 💬"),
    POST_COMMENT_JAKYUNG(NotificationType.POST_COMMENT, "댓글에 응답해주세요~!"),
    POST_COMMENT_KWANGMIN(NotificationType.POST_COMMENT, "댓글 확인 부탁드립니다."),
    POST_COMMENT_EUNHAE(NotificationType.POST_COMMENT, "댓글 도착 💌"),

    // ===== 게시글 좋아요 알림 =====
    POST_REACTION_AREUM(NotificationType.POST_REACTION, "내 글에 반응 등장 💌"),
    POST_REACTION_JAKYUNG(NotificationType.POST_REACTION, "공감한 사람이 누구일까요~?!"),
    POST_REACTION_KWANGMIN(NotificationType.POST_REACTION, "공감 왕이시네요!"),
    POST_REACTION_EUNHAE(NotificationType.POST_REACTION, "게시글 ♥️ + 1"),

    // ===== 답글 알림 =====
    COMMENT_REPLY_AREUM(NotificationType.COMMENT_REPLY, "내 댓글에 대화 이어지는 중! 🧑🏻‍❤️‍🧑🏻"),
    COMMENT_REPLY_JAKYUNG(NotificationType.COMMENT_REPLY, "응답에 응답해볼까요~!?"),
    COMMENT_REPLY_KWANGMIN(NotificationType.COMMENT_REPLY, "뭐야! 댓글에 이렇게 인기가 많아요?"),
    COMMENT_REPLY_EUNHAE(NotificationType.COMMENT_REPLY, "댓글 관심 폭발! 💬"),

    // ===== 댓글 좋아요 알림 =====
    COMMENT_REACTION_AREUM(NotificationType.COMMENT_REACTION, "내 글에 반응 등장 💌"),
    COMMENT_REACTION_JAKYUNG(NotificationType.COMMENT_REACTION, "공감한 사람이 누구일까요~?!"),
    COMMENT_REACTION_KWANGMIN(NotificationType.COMMENT_REACTION, "공감 왕이시네요."),
    COMMENT_REACTION_EUNHAE(NotificationType.COMMENT_REACTION, "댓글 ♥️ + 1"),

    // ===== 멘션 알림 =====
    COMMENT_MENTION_AREUM(NotificationType.COMMENT_MENTION, "{name}님! 여기서 소환됐어요 👀"),
    COMMENT_MENTION_JAKYUNG(NotificationType.COMMENT_MENTION, "{name}님, 멘션 확인 부탁드립니다~!"),
    COMMENT_MENTION_KWANGMIN(NotificationType.COMMENT_MENTION, "{name}님! 빠르게 확인 부탁드려요."),
    COMMENT_MENTION_EUNHAE(NotificationType.COMMENT_MENTION, "똑똑!👊🏻{name}님"),

    // ===== 스터디 신청 알림 =====
    STUDY_APPLICATION_AREUM(NotificationType.STUDY_APPLICATION, "새로운 동료 입장! 바로 확인해 보세요 🙌"),
    STUDY_APPLICATION_JAKYUNG(NotificationType.STUDY_APPLICATION, "스터디 인원이 추가되었어요! 빠르게 확인해주세요!"),
    STUDY_APPLICATION_KWANGMIN(NotificationType.STUDY_APPLICATION, "스터디에 새로운 분이 참여 신청했어요."),

    // ===== 스터디 승인 알림 =====
    STUDY_APPROVED_AREUM(NotificationType.STUDY_APPROVED, "스터디 신청이 승인되었어요! 🎉"),
    STUDY_APPROVED_JAKYUNG(NotificationType.STUDY_APPROVED, "팀원들과 일정에 맞춰 스터디 진행해주세요~! 파이팅! 💪🏻"),
    STUDY_APPROVED_KWANGMIN(NotificationType.STUDY_APPROVED, "스터디 신청 승인 완료!!"),

    // ===== 스터디 삭제 알림 =====
    STUDY_DELETED_AREUM(NotificationType.STUDY_DELETED, "스터디 취소 😭, 다른 스터디에서 다시 만나요!"),
    STUDY_DELETED_JAKYUNG(NotificationType.STUDY_DELETED, "아쉽지만, 다른 스터디에 참여해보시죠 🥲"),
    STUDY_DELETED_KWANGMIN(NotificationType.STUDY_DELETED, "스터디 모집이 취소되었습니다."),
    STUDY_DELETED_EUNHAE(NotificationType.STUDY_DELETED, "🚨스터디 모집 취소 🚨"),
    ;

    companion object {
        private val byType: Map<NotificationType, List<NotificationMessageVariant>> by lazy {
            entries.groupBy { it.type }
        }

        fun getRandomMessage(type: NotificationType): String {
            val variants = byType[type]
                ?: throw IllegalArgumentException("No message variants for type: $type")
            return variants.random().message
        }

        fun getRandomMessage(type: NotificationType, placeholders: Map<String, String>): String {
            var message = getRandomMessage(type)
            placeholders.forEach { (key, value) ->
                message = message.replace("{$key}", value)
            }
            return message
        }

        fun getAllMessages(type: NotificationType): List<String> {
            return byType[type]?.map { it.message } ?: emptyList()
        }
    }
}
