package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

@Embeddable
class TeamRetrospective private constructor(
    retrospectiveGood: String,
    retrospectiveImprove: String,
    retrospectiveNextAction: String,
) {

    @Column(name = "retrospective_good", nullable = false, length = MAX_RETROSPECTIVE_LENGTH)
    var retrospectiveGood: String = retrospectiveGood
        protected set

    @Column(name = "retrospective_improve", nullable = false, length = MAX_RETROSPECTIVE_LENGTH)
    var retrospectiveImprove: String = retrospectiveImprove
        protected set

    @Column(name = "retrospective_next_action", nullable = false, length = MAX_RETROSPECTIVE_LENGTH)
    var retrospectiveNextAction: String = retrospectiveNextAction
        protected set

    companion object {
        const val MIN_RETROSPECTIVE_LENGTH = 2
        const val MAX_RETROSPECTIVE_LENGTH = 1000

        fun of(
            retrospectiveGood: String,
            retrospectiveImprove: String,
            retrospectiveNextAction: String,
        ): TeamRetrospective {
            return TeamRetrospective(
                retrospectiveGood = validateAndNormalize(retrospectiveGood),
                retrospectiveImprove = validateAndNormalize(retrospectiveImprove),
                retrospectiveNextAction = validateAndNormalize(retrospectiveNextAction),
            )
        }

        private fun validateAndNormalize(content: String): String {
            val normalized = content.trim()
            if (normalized.length !in MIN_RETROSPECTIVE_LENGTH..MAX_RETROSPECTIVE_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.STUDY_REPORT_TEAM_RETROSPECTIVE_INVALID)
            }
            return normalized
        }
    }

    fun overwrite(
        retrospectiveGood: String,
        retrospectiveImprove: String,
        retrospectiveNextAction: String,
    ) {
        this.retrospectiveGood = validateAndNormalize(retrospectiveGood)
        this.retrospectiveImprove = validateAndNormalize(retrospectiveImprove)
        this.retrospectiveNextAction = validateAndNormalize(retrospectiveNextAction)
    }
}
