package kr.co.wground.notification.domain.enums

enum class NotificationType(
    val slackCategory: SlackChannelCategory,
) {
    // 게시글 관련
    POST_COMMENT(SlackChannelCategory.GENERAL),
    POST_REACTION(SlackChannelCategory.GENERAL),

    // 댓글 관련
    COMMENT_REPLY(SlackChannelCategory.GENERAL),
    COMMENT_REACTION(SlackChannelCategory.GENERAL),

    // 공지사항
    ANNOUNCEMENT(SlackChannelCategory.GENERAL),

    // 스터디 관련
    STUDY_APPLICATION(SlackChannelCategory.STUDY),
    STUDY_APPROVED(SlackChannelCategory.STUDY),
    STUDY_DELETED(SlackChannelCategory.STUDY),
    STUDY_REPORT_SUBMITTED(SlackChannelCategory.STUDY),
    STUDY_REPORT_RESUBMITTED(SlackChannelCategory.STUDY),
    STUDY_REPORT_APPROVED(SlackChannelCategory.STUDY),
    STUDY_REPORT_REJECTED(SlackChannelCategory.STUDY),
    STUDY_RECRUIT_START(SlackChannelCategory.STUDY),
    STUDY_RECRUIT_END(SlackChannelCategory.STUDY),

    // 멘션
    COMMENT_MENTION(SlackChannelCategory.GENERAL),

    //이력서 첨삭
    RESUME_REVIEW_COMPLETED(SlackChannelCategory.GENERAL)
    ;
}
