package kr.co.wground.study.presentation.request.study_report

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyReportUpsertCommand
import kr.co.wground.study.domain.TeamRetrospective
import kr.co.wground.study.domain.WeeklyActivities

data class StudyReportUpsertRequest(
    @field:NotBlank(message = "1주차 활동 내역은 비어있을 수 없습니다.")
    @field:Size(min = WeeklyActivities.MIN_ACTIVITY_LENGTH, max = WeeklyActivities.MAX_ACTIVITY_LENGTH)
    val week1Activity: String,

    @field:NotBlank(message = "2주차 활동 내역은 비어있을 수 없습니다.")
    @field:Size(min = WeeklyActivities.MIN_ACTIVITY_LENGTH, max = WeeklyActivities.MAX_ACTIVITY_LENGTH)
    val week2Activity: String,

    @field:NotBlank(message = "3주차 활동 내역은 비어있을 수 없습니다.")
    @field:Size(min = WeeklyActivities.MIN_ACTIVITY_LENGTH, max = WeeklyActivities.MAX_ACTIVITY_LENGTH)
    val week3Activity: String,

    @field:NotBlank(message = "4주차 활동 내역은 비어있을 수 없습니다.")
    @field:Size(min = WeeklyActivities.MIN_ACTIVITY_LENGTH, max = WeeklyActivities.MAX_ACTIVITY_LENGTH)
    val week4Activity: String,

    @field:NotBlank(message = "잘한 점은 비어있을 수 없습니다.")
    @field:Size(min = TeamRetrospective.MIN_RETROSPECTIVE_LENGTH, max = TeamRetrospective.MAX_RETROSPECTIVE_LENGTH)
    val retrospectiveGood: String,

    @field:NotBlank(message = "개선할 점은 비어있을 수 없습니다.")
    @field:Size(min = TeamRetrospective.MIN_RETROSPECTIVE_LENGTH, max = TeamRetrospective.MAX_RETROSPECTIVE_LENGTH)
    val retrospectiveImprove: String,

    @field:NotBlank(message = "다음 액션은 비어있을 수 없습니다.")
    @field:Size(min = TeamRetrospective.MIN_RETROSPECTIVE_LENGTH, max = TeamRetrospective.MAX_RETROSPECTIVE_LENGTH)
    val retrospectiveNextAction: String,
) {
    fun toCommand(studyId: Long, userId: UserId): StudyReportUpsertCommand {
        return StudyReportUpsertCommand(
            studyId = studyId,
            userId = userId,
            week1Activity = week1Activity,
            week2Activity = week2Activity,
            week3Activity = week3Activity,
            week4Activity = week4Activity,
            retrospectiveGood = retrospectiveGood,
            retrospectiveImprove = retrospectiveImprove,
            retrospectiveNextAction = retrospectiveNextAction,
        )
    }
}
