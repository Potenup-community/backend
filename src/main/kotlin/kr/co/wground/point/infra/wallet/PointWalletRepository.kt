package kr.co.wground.point.infra.wallet

import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointWallet
import org.springframework.data.jpa.repository.JpaRepository

interface PointWalletRepository : JpaRepository<PointWallet, UserId> {

    fun findByUserId(userId: UserId): PointWallet?

    fun existsByUserId(userId: UserId): Boolean
}