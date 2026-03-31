package kr.co.wground.track.application.dto

import kr.co.wground.track.domain.constant.TrackType

data class TrackCardinalsDto(
    val trackType: TrackType,
    val cardinals: List<Int>,
)
