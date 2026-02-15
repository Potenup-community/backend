package kr.co.wground.point.infra.wallet

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.QPointWallet.pointWallet
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
class CustomPointWalletRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomPointWalletRepository {

    override fun addBalance(userId: UserId, amount: Long): Long {
        return queryFactory
            .update(pointWallet)
            .set(pointWallet.balance, pointWallet.balance.add(amount))
            .set(pointWallet.lastUpdatedAt, LocalDateTime.now())
            .where(pointWallet.userId.eq(userId))
            .execute()
    }
}