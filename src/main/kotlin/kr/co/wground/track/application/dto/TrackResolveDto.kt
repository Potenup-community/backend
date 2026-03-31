package kr.co.wground.track.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.domain.constant.TrackType

data class TrackResolveDto(
    val trackType: TrackType,
    val cardinal: Int,
    val trackId: TrackId,
)
