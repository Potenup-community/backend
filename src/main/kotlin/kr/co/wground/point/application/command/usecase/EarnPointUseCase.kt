package kr.co.wground.point.application.command.usecase

import kr.co.wground.global.common.UserId

interface EarnPointUseCase {
    fun forWritePost(userId: UserId, postId: Long)
    fun forWriteComment(userId: UserId, commentId: Long)
    fun forReceivePostLike(authorId: UserId, reactorId: UserId)
    fun forReceiveCommentLike(authorId: UserId, reactorId: UserId)
    fun forGivePostLike(reactorId: UserId, postId: Long)
    fun forGiveCommentLike(reactorId: UserId, commentId: Long)
    fun forAttendance(userId: UserId, attendanceId: Long)
    fun forAttendanceStreak(userId: UserId, attendanceId: Long)
    fun forStudyCreate(userId: UserId, studyId: Long)
    fun forStudyJoin(userIds: List<UserId>, studyId: Long)
}
