package kr.co.wground.study_schedule.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months
import kr.co.wground.study_schedule.domain.exception.StudyScheduleDomainErrorCode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
        newMonths: Months?,
        newRecruitStart: LocalDate?,
        newRecruitEnd: LocalDate?,
        newStudyEnd: LocalDate?
    ) {
        val convertedStart = newRecruitStart ?: this.recruitStartDate.toLocalDate()
        val convertedEnd = newRecruitEnd ?: this.recruitEndDate.toLocalDate()
        val convertedStudyEnd = newStudyEnd ?: this.studyEndDate.toLocalDate()

        val months = newMonths ?: this.months
        val recruitStartDate = convertedStart.atStartOfDay()
        val recruitEndDate = convertedEnd.atTime(LocalTime.MAX)
        val studyEndDate = convertedStudyEnd.atTime(LocalTime.MAX)

        validateTimeOrder(
            recruitStartDate,
            recruitEndDate,
            studyEndDate
        )
        this.months = months
        this.recruitStartDate = recruitStartDate
        this.recruitEndDate = recruitEndDate
        this.studyEndDate = studyEndDate
        this.updatedAt = LocalDateTime.now()
    }

    private fun validateTimeOrder(
        recruitStart: LocalDateTime,
        recruitEnd: LocalDateTime,
        studyEnd: LocalDateTime
    ) {
        if (!recruitStart.isBefore(recruitEnd)) {
            throw BusinessException(StudyScheduleDomainErrorCode.STUDY_CANT_START_AFTER_END_DATE)
        }
        if (!recruitEnd.isBefore(studyEnd)) {
            throw BusinessException(StudyScheduleDomainErrorCode.STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE)
        }
    }

    fun validateTrackId(trackId: TrackId) {
        if (!this.trackId.equals(trackId)) {
            throw BusinessException(StudyScheduleDomainErrorCode.STUDY_SCHEDULE_IS_NOT_IN_TRACK)
        }
    }

    fun isMonthAfterPrevious(trackId: TrackId): Boolean {
        if (this.trackId != trackId) return true
        return this.studyEndDate.isBefore(this.recruitStartDate)
    }

    fun isRecruitmentClosed(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(this.recruitEndDate)
    }

    fun isCurrentMonth(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(this.recruitStartDate) && now.isBefore(this.studyEndDate)
    }

    fun isScheduleEnded(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(this.studyEndDate)
    }
}