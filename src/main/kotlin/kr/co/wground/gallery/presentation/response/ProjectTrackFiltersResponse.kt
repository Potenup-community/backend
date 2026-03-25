package kr.co.wground.gallery.presentation.response

import kr.co.wground.gallery.application.usecase.result.TrackFilterResult
import kr.co.wground.global.common.TrackId

data class ProjectTrackFiltersResponse(
    val tracks: List<TrackResponse>,
) {
    data class TrackResponse(
        val trackId: TrackId,
        val trackName: String,
    )

    companion object {
        fun from(results: List<TrackFilterResult>): ProjectTrackFiltersResponse =
            ProjectTrackFiltersResponse(
                tracks = results.map { TrackResponse(it.trackId, it.trackName) }
            )
    }
}
