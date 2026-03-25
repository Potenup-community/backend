package kr.co.wground.point.presentation.request

import kr.co.wground.global.common.UserId

data class AdminGivePointRequest(
    val userId: UserId,
    val amount: Long,
)