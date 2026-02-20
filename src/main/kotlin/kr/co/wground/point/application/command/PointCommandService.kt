package kr.co.wground.point.application.command

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.command.usecase.AdminPointUseCase
import kr.co.wground.point.application.command.usecase.CreateWalletUseCase
import kr.co.wground.point.application.command.usecase.EarnPointUseCase
import kr.co.wground.point.application.command.usecase.PurchasePointUseCase
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.exception.PointErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.orm.ObjectOptimisticLockingFailureException

@Service
class PointCommandService(
    private val executor: PointTransactionExecutor
) : EarnPointUseCase, PurchasePointUseCase, AdminPointUseCase, CreateWalletUseCase {

    companion object {
        const val MAX_RETRY_ATTEMPTS = 3
    }

    override fun createWallets(userIds: List<UserId>) {
        userIds.forEach { executor.createWalletIfNotExists(it) }
    }

    override fun forWritePost(userId: UserId, postId: Long) {
        earn(userId, PointType.WRITE_POST) {
            PointHistory.forWritePost(userId, postId)
        }
    }

    override fun forWriteComment(userId: UserId, commentId: Long) {
        earn(userId, PointType.WRITE_COMMENT) {
            PointHistory.forWriteComment(userId, commentId)
        }
    }

    override fun forReceivePostLike(authorId: UserId, reactorId: UserId) {
        earn(authorId, PointType.RECEIVE_LIKE_POST) {
            PointHistory.forPostLikeReward(authorId, reactorId)
        }
    }

    override fun forReceiveCommentLike(authorId: UserId, reactorId: UserId) {
        earn(authorId, PointType.RECEIVE_LIKE_COMMENT) {
            PointHistory.forCommentLikeReward(authorId, reactorId)
        }
    }

    override fun forGivePostLike(reactorId: UserId, postId: Long) {
        earn(reactorId, PointType.GIVE_LIKE_POST) {
            PointHistory.forGivePostLike(reactorId, postId)
        }
    }

    override fun forGiveCommentLike(reactorId: UserId, commentId: Long) {
        earn(reactorId, PointType.GIVE_LIKE_COMMENT) {
            PointHistory.forGiveCommentLike(reactorId, commentId)
        }
    }

    override fun forAttendance(userId: UserId, attendanceDate: Long) {
        earn(userId, PointType.ATTENDANCE) {
            PointHistory.forAttendance(userId,attendanceDate)
        }
    }

    override fun forAttendanceStreak(userId: UserId, attendanceDate: Long) {
        earn(userId, PointType.ATTENDANCE_STREAK) {
            PointHistory.forAttendanceStreak(userId,attendanceDate)
        }
    }

    override fun forStudyCreate(userId: UserId, studyId: Long) {
        earn(userId, PointType.STUDY_CREATE) {
            PointHistory.forStudyCreate(userId, studyId)
        }
    }

    override fun forStudyJoin(userIds: List<UserId>, studyId: Long) {
        val distinct = userIds.distinct()
        if (distinct.isEmpty()) return

        retryOptimisticOrSkip {
            executor.executeStudyJoinBatchEvent(distinct, studyId)
        }
    }

    override fun forPurchase(userId: UserId, amount: Long, itemId: Long) {
        retryOptimisticOrThrow {
            executor.executePurchase(userId, amount, itemId)
        }
    }

    override fun forUpgradePurchase(userId: UserId, amount: Long, itemId: Long) {
        retryOptimisticOrThrow {
            executor.executeUpgradePurchase(userId, amount, itemId)
        }
    }

    override fun givePoint(userId: UserId, amount: Long, adminId: UserId) {
        retryOptimisticOrThrow {
            executor.executeAdminGive(userId, amount, adminId)
        }
    }

    private fun earn(userId: UserId, type: PointType, historyFactory: () -> PointHistory) {
        retryOptimisticOrSkip {
            executor.executeEarnEvent(userId, type, historyFactory)
        }
    }

    private inline fun <T> retryOptimisticOrThrow(maxAttempts: Int = MAX_RETRY_ATTEMPTS, block: () -> T): T {
        repeat(maxAttempts) { _ ->
            try {
                return block()
            } catch (_: ObjectOptimisticLockingFailureException) {
                // retry
            }
        }
        throw BusinessException(PointErrorCode.POINT_PROCESSING_FAILED)
    }

    private inline fun retryOptimisticOrSkip(maxAttempts: Int = MAX_RETRY_ATTEMPTS, block: () -> Unit) {
        repeat(maxAttempts) {
            try {
                block()
                return
            } catch (_: ObjectOptimisticLockingFailureException) {
                // retry
            } catch (_: DataIntegrityViolationException) {
                return // 이벤트는 중복/경합 스킵
            }
        }
    }
}
