package kr.co.wground.point.application.command.usecase

import kr.co.wground.global.common.UserId

interface AdminPointUseCase {
    fun givePoint(userId: UserId, amount: Long, adminId: Long)
}
