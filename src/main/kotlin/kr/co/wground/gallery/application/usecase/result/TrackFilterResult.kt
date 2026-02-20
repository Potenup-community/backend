package kr.co.wground.gallery.application.usecase.result

import kr.co.wground.global.common.TrackId

data class TrackFilterResult(
    val trackId: TrackId,
    val trackName: String,
)
