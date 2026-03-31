package kr.co.wground.track.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import kr.co.wground.exception.BusinessException
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.domain.exception.TrackDomainErrorCode
import java.time.LocalDate

@Entity
class Track(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val trackId: Long = 0,
    trackType: TrackType? = null,
    cardinal: Int? = null,
    startDate: LocalDate,
    endDate: LocalDate
) {
    @Enumerated(EnumType.STRING)
    var trackType: TrackType? = trackType
        protected set
    var cardinal: Int? = cardinal
        protected set
    var startDate: LocalDate = startDate
        protected set
    var endDate: LocalDate = endDate
        protected set

    @Enumerated(EnumType.STRING)
    var trackStatus: TrackStatus = determineStatus(endDate)
        protected set

    init {
        validateTrackTypeAndCardinal(trackType, cardinal)
        validateTime(startDate, endDate)
    }

    @PreUpdate
    fun onPreUpdate() {
        this.trackStatus = determineStatus(this.endDate, LocalDate.now())
    }

    fun updateTrack(
        trackType: TrackType?,
        cardinal: Int?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        now: LocalDate = LocalDate.now()
    ) {
        val newTrackType = trackType ?: this.trackType
        val newCardinal = when {
            trackType == TrackType.ADMIN -> null
            cardinal != null -> cardinal
            else -> this.cardinal
        }
        validateTrackTypeAndCardinal(newTrackType, newCardinal)
        this.trackType = newTrackType
        this.cardinal = newCardinal

        val newStartDate = startDate ?: this.startDate
        val newEndDate = endDate ?: this.endDate

        validateTime(newStartDate, newEndDate)

        this.startDate = newStartDate
        this.endDate = newEndDate

        this.trackStatus = determineStatus(newEndDate, now)
    }

    fun refreshStatus(now: LocalDate = LocalDate.now()) {
        this.trackStatus = determineStatus(this.endDate, now)
    }

    fun displayName(): String {
        val type = this.trackType
        val num = this.cardinal
        return if (type != null && type != TrackType.ADMIN && num != null) {
            "${type.displayName} ${num}기"
        } else if (type == TrackType.ADMIN) {
            TrackType.ADMIN.displayName
        } else {
            ""
        }
    }

    fun isAdminTrack(): Boolean {
        return this.trackType == TrackType.ADMIN
    }

    private fun determineStatus(targetEndDate: LocalDate, now: LocalDate = LocalDate.now()): TrackStatus {
        return if (targetEndDate >= now) TrackStatus.ENROLLED else TrackStatus.GRADUATED
    }

    private fun validateTrackTypeAndCardinal(trackType: TrackType?, cardinal: Int?) {
        if (trackType == null && cardinal == null) return

        if (trackType == TrackType.ADMIN && cardinal != null) {
            throw BusinessException(TrackDomainErrorCode.INVALID_TRACK_INPUT)
        }
        if (trackType != null && trackType != TrackType.ADMIN && (cardinal == null || cardinal <= 0)) {
            throw BusinessException(TrackDomainErrorCode.INVALID_TRACK_INPUT)
        }
        if (trackType == null && cardinal != null) {
            throw BusinessException(TrackDomainErrorCode.INVALID_TRACK_INPUT)
        }
    }

    private fun validateTime(start: LocalDate, end: LocalDate) {
        if (start.isAfter(end)) {
            throw BusinessException(TrackDomainErrorCode.INVALID_DATE_RANGE)
        }
    }
}
