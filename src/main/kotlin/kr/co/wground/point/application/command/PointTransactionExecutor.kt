package kr.co.wground.point.application.command

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointReferenceType
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.PointWallet
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.history.PointHistoryRepository
import kr.co.wground.point.infra.wallet.PointWalletRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class PointTransactionExecutor(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointWalletRepository: PointWalletRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createWalletIfNotExists(userId: UserId) {
        getOrCreateWallet(userId)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun executeEarnEvent(userId: UserId, type: PointType, historyFactory: () -> PointHistory) {
        if (!canEarnToday(userId, type)) return

        val history = historyFactory()
        pointHistoryRepository.save(history)

        val wallet = getOrCreateWallet(userId)
        wallet.addBalance(history.amount)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun executeStudyJoinBatchEvent(userIds: List<UserId>, studyId: Long) {
        val target = targetStudyJoinUsers(userIds, studyId)
        if (target.isEmpty()) return

        val histories = target.map { PointHistory.forStudyJoin(it, studyId) }
        pointHistoryRepository.saveAll(histories)

        val amount = PointType.STUDY_JOIN.amount
        val walletMap = pointWalletRepository.findByUserIdIn(target).associateBy { it.userId }

        val missing = target
            .filterNot { walletMap.containsKey(it) }
            .map { PointWallet.create(it) }

        if (missing.isNotEmpty()) {
            try {
                pointWalletRepository.saveAll(missing)
            } catch (_: DataIntegrityViolationException) {
                // skip
            }
        }

        val allWallets = pointWalletRepository.findByUserIdIn(target).associateBy { it.userId }
        target.forEach { userId ->
            allWallets[userId]?.addBalance(amount)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun executePurchase(userId: UserId, amount: Long, itemId: Long) {
        val wallet = pointWalletRepository.findByUserId(userId)
            ?: throw BusinessException(PointErrorCode.WALLET_NOT_FOUND)

        wallet.deductBalance(amount)

        pointHistoryRepository.save(
            PointHistory.forPurchase(userId, amount, itemId)
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun executeAdminGive(userId: UserId, amount: Long, adminId: Long) {
        val wallet = getOrCreateWallet(userId)

        pointHistoryRepository.save(
            PointHistory.forAdminGiven(userId, amount, adminId)
        )
        wallet.addBalance(amount)
    }

    private fun getOrCreateWallet(userId: UserId): PointWallet {
        pointWalletRepository.findByUserId(userId)?.let { return it }
        return try {
            pointWalletRepository.save(PointWallet.create(userId))
        } catch (_: DataIntegrityViolationException) {
            pointWalletRepository.findByUserId(userId)
                ?: throw BusinessException(PointErrorCode.WALLET_NOT_FOUND)
        }
    }

    private fun canEarnToday(userId: UserId, type: PointType): Boolean {
        val limit = type.dailyLimit ?: return true

        val today = LocalDate.now()
        val start = today.atStartOfDay()
        val end = today.plusDays(1).atStartOfDay()

        val todayCount = pointHistoryRepository.countDailyByUserIdAndType(userId, type, start, end)
        return todayCount < limit
    }

    private fun targetStudyJoinUsers(userIds: List<UserId>, studyId: Long): List<UserId> {
        if (userIds.isEmpty()) return emptyList()

        val already = pointHistoryRepository.findUserIdsWithHistory(
            userIds,
            PointReferenceType.STUDY,
            studyId,
            PointType.STUDY_JOIN
        ).toSet()

        return (userIds - already).toList()
    }
}
