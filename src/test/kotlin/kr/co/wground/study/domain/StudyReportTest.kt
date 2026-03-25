package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

@DisplayName("스터디 결과 보고(StudyReport) 도메인 테스트")
class StudyReportTest {

    @Test
    @DisplayName("IN_PROGRESS 상태의 스터디에서 결과 보고 생성 시 상태는 SUBMITTED 이다")
    fun shouldCreateSubmittedReport_whenStudyInProgress() {
        val study = createStudyWithStatus(StudyStatus.IN_PROGRESS)

        val report = StudyReport.create(
            study = study,
            weeklyActivities = validWeeklyActivities(),
            teamRetrospective = validTeamRetrospective(),
        )

        assertEquals(StudyReportApprovalStatus.SUBMITTED, report.status)
    }

    @Test
    @DisplayName("RECRUITING 상태의 스터디에서 결과 보고 생성 시 예외 발생 - BusinessException(STUDY_REPORT_UPDATE_NOT_ALLOWED_FOR_STUDY_STATUS)")
    fun shouldThrowUpdateNotAllowed_whenCreateReportOnRecruitingStudy() {
        val study = createStudyWithStatus(StudyStatus.RECRUITING)

        val thrown = assertThrows<BusinessException> {
            StudyReport.create(
                study = study,
                weeklyActivities = validWeeklyActivities(),
                teamRetrospective = validTeamRetrospective(),
            )
        }

        assertEquals(StudyDomainErrorCode.STUDY_REPORT_UPDATE_NOT_ALLOWED_FOR_STUDY_STATUS.code, thrown.code)
    }

    @Test
    @DisplayName("REJECTED 상태의 결과 보고를 재상신하면 상태는 RESUBMITTED 이다")
    fun shouldChangeToResubmitted_whenResubmitRejectedReport() {
        val study = createStudyWithStatus(StudyStatus.IN_PROGRESS)
        val report = StudyReport.create(
            study = study,
            weeklyActivities = validWeeklyActivities(),
            teamRetrospective = validTeamRetrospective(),
        )
        report.markRejected("보완이 필요합니다.")

        report.markResubmitted()

        assertEquals(StudyReportApprovalStatus.RESUBMITTED, report.status)
    }

    @Test
    @DisplayName("SUBMITTED 상태에서 재상신 시도 시 예외 발생 - BusinessException(STUDY_REPORT_STATUS_TRANSITION_INVALID)")
    fun shouldThrowStatusTransitionInvalid_whenResubmitFromSubmitted() {
        val study = createStudyWithStatus(StudyStatus.IN_PROGRESS)
        val report = StudyReport.create(
            study = study,
            weeklyActivities = validWeeklyActivities(),
            teamRetrospective = validTeamRetrospective(),
        )

        val thrown = assertThrows<BusinessException> {
            report.markResubmitted()
        }

        assertEquals(StudyDomainErrorCode.STUDY_REPORT_STATUS_TRANSITION_INVALID.code, thrown.code)
    }

    @Test
    @DisplayName("반려 사유가 공백인 경우 예외 발생 - BusinessException(STUDY_REPORT_REJECT_REASON_REQUIRED)")
    fun shouldThrowRejectReasonRequired_whenRejectReasonBlank() {
        val study = createStudyWithStatus(StudyStatus.IN_PROGRESS)
        val report = StudyReport.create(
            study = study,
            weeklyActivities = validWeeklyActivities(),
            teamRetrospective = validTeamRetrospective(),
        )

        val thrown = assertThrows<BusinessException> {
            report.markRejected("   ")
        }

        assertEquals(StudyDomainErrorCode.STUDY_REPORT_REJECT_REASON_REQUIRED.code, thrown.code)
    }

    // ----- factories

    private fun createStudyWithStatus(status: StudyStatus): Study {
        val now = LocalDateTime.now()
        return Study.loadFromDb(
            id = 1L,
            name = "스터디",
            leaderId = 1L,
            trackId = 1L,
            scheduleId = 1L,
            description = "스터디 설명",
            status = status,
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "피자 먹기",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            createdAt = now.minusDays(1),
            updatedAt = now,
        )
    }

    private fun validWeeklyActivities(): WeeklyActivities {
        return WeeklyActivities.of(
            week1Activity = "1주차 활동",
            week2Activity = "2주차 활동",
            week3Activity = "3주차 활동",
            week4Activity = "4주차 활동",
        )
    }

    private fun validTeamRetrospective(): TeamRetrospective {
        return TeamRetrospective.of(
            retrospectiveGood = "잘한 점",
            retrospectiveImprove = "개선할 점",
            retrospectiveNextAction = "다음 액션",
        )
    }
}
