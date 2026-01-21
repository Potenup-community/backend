package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate
import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.constant.Months
import java.time.LocalDateTime
import java.time.LocalTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_track_months",
            columnNames = ["track_id", "months"]
        )
    ]
)
class StudySchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    trackId: TrackId,
    months: Months,
    recruitStartDate: LocalDate,
    recruitEndDate: LocalDate,
    studyEndDate: LocalDate,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(name = "track_id", nullable = false)
    var trackId: TrackId = trackId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var months: Months = months
        protected set

    @Column(nullable = false)
    var recruitStartDate: LocalDateTime = recruitStartDate.atStartOfDay()
        protected set

    @Column(nullable = false)
    var recruitEndDate: LocalDateTime = recruitEndDate.atTime(LocalTime.MAX)
        protected set

    @Column(nullable = false)
    var studyEndDate: LocalDateTime = studyEndDate.atTime(LocalTime.MAX)
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    init {
        validateTimeOrder(
            recruitStart = this.recruitStartDate,
            recruitEnd = this.recruitEndDate,
            studyEnd = this.studyEndDate
        )
    }

    fun updateSchedule(
        newRecruitStart: LocalDate,
        newRecruitEnd: LocalDate,
        newStudyEnd: LocalDate
    ) {
        val convertedStart = newRecruitStart.atStartOfDay()
        val convertedEnd = newRecruitEnd.atTime(LocalTime.MAX)
        val convertedStudyEnd = newStudyEnd.atTime(LocalTime.MAX)

        validateTimeOrder(
            convertedStart,
            convertedEnd,
            convertedStudyEnd
        )

        this.recruitStartDate = convertedStart
        this.recruitEndDate = convertedEnd
        this.studyEndDate = convertedStudyEnd
        this.updatedAt = LocalDateTime.now()
    }

    private fun validateTimeOrder(
        recruitStart: LocalDateTime,
        recruitEnd: LocalDateTime,
        studyEnd: LocalDateTime
    ) {
        if (!recruitStart.isBefore(recruitEnd)) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANT_START_AFTER_END_DATE)
        }
        if (!recruitEnd.isBefore(studyEnd)) {
            throw BusinessException(StudyDomainErrorCode.STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE)
        }
    }

    fun isMonthAfterPrevious(nextSchedule: StudySchedule): Boolean {
        if (this.trackId != nextSchedule.trackId) return true
        return this.studyEndDate.isBefore(nextSchedule.recruitStartDate)
    }

    fun isRecruitmentClosed(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(this.recruitEndDate)
    }
}