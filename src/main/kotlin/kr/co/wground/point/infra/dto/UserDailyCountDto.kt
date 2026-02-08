package kr.co.wground.point.infra.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointType

data class UserDailyCountDto(
    val userId: UserId,
    val type: PointType,
    val count: Long
)