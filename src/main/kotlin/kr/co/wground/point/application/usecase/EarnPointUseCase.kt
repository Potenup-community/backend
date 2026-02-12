package kr.co.wground.point.application.usecase

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.PointWallet
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.history.PointHistoryRepository
import kr.co.wground.point.infra.wallet.PointWalletRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
@Transactional
class EarnPointUseCase(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointWalletRepository: PointWalletRepository
) {
    // 콘텐츠 작성
    fun forWritePost(userId: UserId, postId: Long) {
        earnWithDailyLimit(userId, PointType.WRITE_POST) {
            PointHistory.forWritePost(userId, postId)
        }
    }

    fun forWriteComment(userId: UserId, commentId: Long) {
        earnWithDailyLimit(userId, PointType.WRITE_COMMENT) {
            PointHistory.forWriteComment(userId, commentId)
        }
    }

    // 좋아요 받음 (RECEIVE)
    fun forReceivePostLike(authorId: UserId, reactorId: UserId) {
        earnWithDailyLimit(authorId, PointType.RECEIVE_LIKE_POST) {
            PointHistory.forPostLikeReward(authorId, reactorId)
        }
    }

    fun forReceiveCommentLike(authorId: UserId, reactorId: UserId) {
        earnWithDailyLimit(authorId, PointType.RECEIVE_LIKE_COMMENT) {
            PointHistory.forCommentLikeReward(authorId, reactorId)
        }
    }

    // 좋아요 누름 (GIVE)
    fun forGivePostLike(reactorId: UserId, postId: Long) {
        earnWithDailyLimit(reactorId, PointType.GIVE_LIKE_POST) {
            PointHistory.forGivePostLike(reactorId, postId)
        }
    }

    fun forGiveCommentLike(reactorId: UserId, commentId: Long) {
        earnWithDailyLimit(reactorId, PointType.GIVE_LIKE_COMMENT) {
            PointHistory.forGiveCommentLike(reactorId, commentId)
        }
    }

    // 출석
    fun forAttendance(userId: UserId, attendanceId: Long) {
        earnWithDailyLimit(userId, PointType.ATTENDANCE) {
            PointHistory.forAttendance(userId, attendanceId)
        }
    }

    fun forAttendanceStreak(userId: UserId, attendanceId: Long) {
        earnWithDailyLimit(userId, PointType.ATTENDANCE_STREAK) {
            PointHistory.forAttendanceStreak(userId, attendanceId)
        }
    }

    // 스터디 (한도 없음)
    fun forStudyCreate(userId: UserId, studyId: Long) {
        earn(userId, PointHistory.forStudyCreate(userId, studyId))
    }

    fun forStudyJoin(userId: UserId, studyId: Long) {
        earn(userId, PointHistory.forStudyJoin(userId, studyId))
    }

    // 하루 수집 포인트 체크 후 포인트 지급
    private fun earnWithDailyLimit(userId: UserId, type: PointType, historyFactory: () -> PointHistory) {
        val dailyLimit = type.dailyLimit
            ?: return earn(userId, historyFactory())

        val today = LocalDate.now()
        val todayCount = pointHistoryRepository.countDailyByUserIdAndType(
            userId, type, today.atStartOfDay(), today.plusDays(1).atStartOfDay()
        )

        validateExceededDailyLimit(todayCount, dailyLimit)

        earn(userId, historyFactory())
    }

    private fun earn(userId: UserId, history: PointHistory) {
        validateAlreadyReceived(history)
        pointHistoryRepository.save(history)

        val wallet = getOrCreateWallet(userId)
        wallet.addBalance(history.amount)
    }

    private fun getOrCreateWallet(userId: UserId): PointWallet {
        return pointWalletRepository.findByUserId(userId)
            ?: pointWalletRepository.save(PointWallet.create(userId))
    }


    private fun validateAlreadyReceived(history: PointHistory) {
        val exists = pointHistoryRepository.existsByUserIdAndRefTypeAndRefIdAndType(
            history.userId, history.refType, history.refId, history.type
        )
        if (exists) {
            throw BusinessException(PointErrorCode.DUPLICATE_POINT_HISTORY)
        }
    }

    private fun validateExceededDailyLimit(todayCount: Long, dailyLimit: Int) {
        if (todayCount >= dailyLimit) {
            throw BusinessException(PointErrorCode.DAILY_LIMIT_EXCEEDED)
        }
    }
}