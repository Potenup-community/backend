package kr.co.wground.study.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.constant.Months
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class StudySchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    trackId: TrackId,
    months: Months,
    startDate: LocalDate,
    recruitEndDate: LocalDate,
    studyEndDate: LocalDate,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    var trackId: TrackId = trackId
        protected set
    var months: Months = months
        protected set
    var startDate: LocalDate = startDate
        protected set
    var recruitEndDate: LocalDate = recruitEndDate
        protected set
    var studyEndDate: LocalDate = studyEndDate
        protected set
    var updatedAt: LocalDateTime = updatedAt
        protected set
}