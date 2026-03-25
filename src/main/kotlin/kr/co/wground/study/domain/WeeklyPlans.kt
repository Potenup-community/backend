package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

@Embeddable
class WeeklyPlans private constructor(
    week1Plan: String,
    week2Plan: String,
    week3Plan: String,
    week4Plan: String,
) {

    @Column(name = "week_1_plan", nullable = false, length = MAX_PLAN_LENGTH)
    var week1Plan: String = week1Plan
        protected set

    @Column(name = "week_2_plan", nullable = false, length = MAX_PLAN_LENGTH)
    var week2Plan: String = week2Plan
        protected set

    @Column(name = "week_3_plan", nullable = false, length = MAX_PLAN_LENGTH)
    var week3Plan: String = week3Plan
        protected set

    @Column(name = "week_4_plan", nullable = false, length = MAX_PLAN_LENGTH)
    var week4Plan: String = week4Plan
        protected set

    companion object {
        const val MIN_PLAN_LENGTH = 2
        const val MAX_PLAN_LENGTH = 300

        fun of(
            week1Plan: String,
            week2Plan: String,
            week3Plan: String,
            week4Plan: String,
        ): WeeklyPlans {
            return WeeklyPlans(
                week1Plan = validateAndNormalize(week1Plan),
                week2Plan = validateAndNormalize(week2Plan),
                week3Plan = validateAndNormalize(week3Plan),
                week4Plan = validateAndNormalize(week4Plan),
            )
        }

        private fun validateAndNormalize(plan: String): String {
            val normalized = plan.trim()
            if (normalized.length !in MIN_PLAN_LENGTH..MAX_PLAN_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.WEEKLY_STUDY_PLANS_INVALID)
            }
            return normalized
        }
    }

    fun toList(): List<String> {
        return listOf(week1Plan, week2Plan, week3Plan, week4Plan)
    }

    fun overwrite(
        week1Plan: String,
        week2Plan: String,
        week3Plan: String,
        week4Plan: String,
    ) {
        this.week1Plan = validateAndNormalize(week1Plan)
        this.week2Plan = validateAndNormalize(week2Plan)
        this.week3Plan = validateAndNormalize(week3Plan)
        this.week4Plan = validateAndNormalize(week4Plan)
    }
}
