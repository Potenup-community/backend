package kr.co.wground.track.application.event

import java.time.LocalDate

data class TrackChangedEvent(
    val trackId: Long,
    val endDate: LocalDate,
    val type: EventType
) {
    enum class EventType { CREATED, UPDATED, DELETED }
}
