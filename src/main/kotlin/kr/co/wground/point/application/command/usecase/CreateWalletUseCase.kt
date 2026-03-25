package kr.co.wground.point.application.command.usecase

import kr.co.wground.global.common.UserId

interface CreateWalletUseCase {
    fun createWallets(userIds: List<UserId>)
}