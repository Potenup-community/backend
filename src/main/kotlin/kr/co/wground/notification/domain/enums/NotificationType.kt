package kr.co.wground.notification.domain.enums

enum class NotificationType {
    // 게시글 관련
    POST_COMMENT,
    POST_REACTION,

    // 댓글 관련
    COMMENT_REPLY,
    COMMENT_REACTION,

    // 스터디 관련
    STUDY_APPLICATION,
    STUDY_APPROVED,
    STUDY_DELETED,

    // 멘션
    COMMENT_MENTION,

    // 공지사항
    ANNOUNCEMENT,

    // 스터디 모집
    STUDY_RECRUIT_START,
    STUDY_RECRUIT_END,
    ;
}
