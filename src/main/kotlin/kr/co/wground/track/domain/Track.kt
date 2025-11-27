package kr.co.wground.track.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import kr.co.wground.exception.BusinessException
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.domain.exception.TrackDomainErrorCode
import java.time.LocalDate

@Entity
class Track(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val trackId: Long = 0,
    trackName: String,
    startDate: LocalDate,
    endDate: LocalDate
) {
    var trackName: String = trackName
        protected set
    var startDate: LocalDate = startDate
        protected set
    var endDate: LocalDate = endDate
        protected set

    @Enumerated(EnumType.STRING)
    var trackStatus: TrackStatus = determineStatus(endDate)
        protected set

    init {
        if (trackName.isBlank()) {
            throw BusinessException(TrackDomainErrorCode.TRACK_NAME_IS_BLANK)
        }
        validateTime(startDate, endDate)
    }

    @PrePersist
    @PreUpdate
    fun onPersist() {
        this.trackStatus = determineStatus(this.endDate, LocalDate.now())
    }

    fun updateTrack(
        trackName: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        now: LocalDate = LocalDate.now()
    ) {
        trackName?.let {
            if (it.isBlank()) throw BusinessException(TrackDomainErrorCode.TRACK_NAME_IS_BLANK)
            this.trackName = it
        }

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

    private fun determineStatus(targetEndDate: LocalDate, now: LocalDate = LocalDate.now()): TrackStatus {
        return if (targetEndDate >= now) TrackStatus.ENROLLED else TrackStatus.GRADUATED
    }

    private fun validateTime(start: LocalDate, end: LocalDate) {
        if (start.isAfter(end)) {
            throw BusinessException(TrackDomainErrorCode.INVALID_DATE_RANGE)
        }
    }
}
