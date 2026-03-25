package kr.co.wground.study.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime
import kr.co.wground.shop.application.dto.EquippedItem

data class ParticipantInfo(
    val id: UserId,
    val name: String,
    val trackId: TrackId,
    val trackName: String,
    val joinedAt: LocalDateTime,
    val profileImageUrl: String,
)
