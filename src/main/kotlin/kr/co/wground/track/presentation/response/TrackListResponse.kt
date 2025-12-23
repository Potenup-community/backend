package kr.co.wground.track.presentation.response


data class TrackListResponse<T>(
    val content: List<T>,
)

