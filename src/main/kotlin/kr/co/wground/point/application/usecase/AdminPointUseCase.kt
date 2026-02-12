package kr.co.wground.point.application.usecase

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointWallet
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.history.PointHistoryRepository
import kr.co.wground.point.infra.wallet.PointWalletRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AdminPointUseCase(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointWalletRepository: PointWalletRepository
) {
    fun givePoint(userId: UserId, amount: Long, eventId: Long) {
        validateGivenAmount(amount)

        val wallet = pointWalletRepository.findByUserId(userId)
            ?: pointWalletRepository.save(PointWallet.create(userId))

        wallet.addBalance(amount)

        pointHistoryRepository.save(
            PointHistory.forAdminGiven(userId, amount, eventId)
        )
    }

    private fun validateGivenAmount(amount: Long) {
        if (amount <= 0) throw BusinessException(PointErrorCode.INVALID_AMOUNT)
    }
}