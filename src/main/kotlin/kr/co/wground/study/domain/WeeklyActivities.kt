package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

@Embeddable
class WeeklyActivities private constructor(
    week1Activity: String,
    week2Activity: String,
    week3Activity: String,
    week4Activity: String,
) {

    @Column(name = "week_1_activity", nullable = false, length = MAX_ACTIVITY_LENGTH)
    var week1Activity: String = week1Activity
        protected set

    @Column(name = "week_2_activity", nullable = false, length = MAX_ACTIVITY_LENGTH)
    var week2Activity: String = week2Activity
        protected set

    @Column(name = "week_3_activity", nullable = false, length = MAX_ACTIVITY_LENGTH)
    var week3Activity: String = week3Activity
        protected set

    @Column(name = "week_4_activity", nullable = false, length = MAX_ACTIVITY_LENGTH)
    var week4Activity: String = week4Activity
        protected set

    companion object {
        const val MIN_ACTIVITY_LENGTH = 2
        const val MAX_ACTIVITY_LENGTH = 500

        fun of(
            week1Activity: String,
            week2Activity: String,
            week3Activity: String,
            week4Activity: String,
        ): WeeklyActivities {
            return WeeklyActivities(
                week1Activity = validateAndNormalize(week1Activity),
                week2Activity = validateAndNormalize(week2Activity),
                week3Activity = validateAndNormalize(week3Activity),
                week4Activity = validateAndNormalize(week4Activity),
            )
        }

        private fun validateAndNormalize(activity: String): String {
            val normalized = activity.trim()
            if (normalized.length !in MIN_ACTIVITY_LENGTH..MAX_ACTIVITY_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.STUDY_REPORT_WEEKLY_ACTIVITIES_INVALID)
            }
            return normalized
        }
    }

    fun overwrite(
        week1Activity: String,
        week2Activity: String,
        week3Activity: String,
        week4Activity: String,
    ) {
        this.week1Activity = validateAndNormalize(week1Activity)
        this.week2Activity = validateAndNormalize(week2Activity)
        this.week3Activity = validateAndNormalize(week3Activity)
        this.week4Activity = validateAndNormalize(week4Activity)
    }
}
