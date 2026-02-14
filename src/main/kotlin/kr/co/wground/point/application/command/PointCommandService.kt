package kr.co.wground.point.application.command

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.command.usecase.AdminPointUseCase
import kr.co.wground.point.application.command.usecase.EarnPointUseCase
import kr.co.wground.point.application.command.usecase.PurchasePointUseCase
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.PointWallet
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.history.PointHistoryRepository
import kr.co.wground.point.infra.wallet.PointWalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kr.co.wground.point.domain.PointReferenceType

@Service
@Transactional
class PointCommandService(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointWalletRepository: PointWalletRepository
) : EarnPointUseCase, PurchasePointUseCase, AdminPointUseCase {

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

    override fun forAttendance(userId: UserId, attendanceId: Long) {
        earn(userId, PointType.ATTENDANCE) {
            PointHistory.forAttendance(userId, attendanceId)
        }
    }

    override fun forAttendanceStreak(userId: UserId, attendanceId: Long) {
        earn(userId, PointType.ATTENDANCE_STREAK) {
            PointHistory.forAttendanceStreak(userId, attendanceId)
        }
    }

    override fun forStudyCreate(userId: UserId, studyId: Long) {
        earn(userId, PointType.STUDY_CREATE) {
            PointHistory.forStudyCreate(userId, studyId)
        }
    }

    override fun forStudyJoin(userIds: List<UserId>, studyId: Long) {
        val alreadyEarned = pointHistoryRepository.findUserIdsWithHistory(
            userIds, PointReferenceType.STUDY, studyId, PointType.STUDY_JOIN
        )

        val targetUserIds = userIds - alreadyEarned.toSet()
        if (targetUserIds.isEmpty()) return

        val wallets = pointWalletRepository.findByUserIdIn(targetUserIds)
        val joinAmount = PointType.STUDY_JOIN.amount
        val walletMap = wallets.associateBy { it.userId }

        targetUserIds.forEach { userId ->
            val wallet = walletMap[userId]
                ?: pointWalletRepository.save(PointWallet.create(userId))

            wallet.addBalance(joinAmount)
        }

        val histories = targetUserIds.map { userId ->
            PointHistory.forStudyJoin(userId, studyId)
        }

        pointHistoryRepository.saveAll(histories)
    }

    override fun forPurchase(userId: UserId, amount: Long, itemId: Long) {
        val wallet = pointWalletRepository.findByUserId(userId)
            ?: throw BusinessException(PointErrorCode.WALLET_NOT_FOUND)

        wallet.deductBalance(amount)

        pointHistoryRepository.save(
            PointHistory.forPurchase(userId, amount, itemId)
        )
    }

    override fun givePoint(userId: UserId, amount: Long, adminId: Long) {
        val wallet = pointWalletRepository.findByUserId(userId)
            ?: pointWalletRepository.save(PointWallet.create(userId))

        wallet.addBalance(amount)

        pointHistoryRepository.save(
            PointHistory.forAdminGiven(userId, amount, adminId)
        )
    }

    private fun earn(
        userId: UserId,
        type: PointType,
        historyFactory: () -> PointHistory
    ) {
        validateDailyLimit(userId, type)
        val history = historyFactory().also { it.validateNotDuplicate(pointHistoryRepository) }

        pointHistoryRepository.save(history)
        getOrCreateWallet(userId).addBalance(history.amount)
    }

    private fun validateDailyLimit(userId: UserId, type: PointType) {
        val limit = type.dailyLimit ?: return

        val today = LocalDate.now()
        val start = today.atStartOfDay()
        val end = today.plusDays(1).atStartOfDay()

        val todayCount = pointHistoryRepository.countDailyByUserIdAndType(userId, type, start, end)

        if (todayCount >= limit) {
            throw BusinessException(PointErrorCode.DAILY_LIMIT_EXCEEDED)
        }
    }

    private fun PointHistory.validateNotDuplicate(repo: PointHistoryRepository) {
        val duplicated = repo.existsByUserIdAndRefTypeAndRefIdAndType(
            userId, refType, refId, type
        )
        if (duplicated) {
            throw BusinessException(PointErrorCode.DUPLICATE_POINT_HISTORY)
        }
    }

    private fun getOrCreateWallet(userId: UserId): PointWallet {
        return pointWalletRepository.findByUserId(userId)
            ?: pointWalletRepository.save(PointWallet.create(userId))
    }
}
