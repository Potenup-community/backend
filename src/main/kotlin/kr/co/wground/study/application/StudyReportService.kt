package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyReportSubmissionStatusQueryResult
import kr.co.wground.study.application.dto.StudyReportUpsertCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyReport
import kr.co.wground.study.domain.TeamRetrospective
import kr.co.wground.study.domain.WeeklyActivities
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import kr.co.wground.study.infra.StudyReportRepository
import kr.co.wground.study.infra.StudyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudyReportService(
    private val studyRepository: StudyRepository,
    private val studyReportRepository: StudyReportRepository,
) {

    /**
     * 스터디 결과 보고서 upsert
     * update 시 RESUBMITTED 로 상태 변경
     */
    fun upsertAndSubmit(command: StudyReportUpsertCommand): Long {
        val study = findStudyByIdOrThrows(command.studyId)
        validateStudyLeader(study, command.userId)

        val weeklyActivities = WeeklyActivities.of(
            week1Activity = command.week1Activity,
            week2Activity = command.week2Activity,
            week3Activity = command.week3Activity,
            week4Activity = command.week4Activity,
        )
        val teamRetrospective = TeamRetrospective.of(
            retrospectiveGood = command.retrospectiveGood,
            retrospectiveImprove = command.retrospectiveImprove,
            retrospectiveNextAction = command.retrospectiveNextAction,
        )

        val existingReport = studyReportRepository.findByStudyId(study.id)
            // 기존 보고서가 없으면 새로 생성 및 저장 후 id 반환
            ?: return studyReportRepository.save(
                StudyReport.create(
                    study = study,
                    weeklyActivities = weeklyActivities,
                    teamRetrospective = teamRetrospective,
                )
            ).id

        // 기존 보고서가 있으면 수정 후
        existingReport.update(
            weeklyActivities = weeklyActivities,
            teamRetrospective = teamRetrospective,
        )

        if (existingReport.status == StudyReportApprovalStatus.REJECTED) {
            existingReport.markResubmitted()
        }

        return existingReport.id
    }

    @Transactional(readOnly = true)
    fun getMySubmissionStatus(studyId: Long, userId: UserId): StudyReportSubmissionStatusQueryResult {
        val study = findStudyByIdOrThrows(studyId)
        validateStudyLeader(study, userId)

        // report 는 null 일 수 있음에 유의
        val report = studyReportRepository.findByStudyId(study.id)
        return StudyReportSubmissionStatusQueryResult.of(report)
    }

    // ----- helpers

    private fun findStudyByIdOrThrows(studyId: Long): Study {
        return studyRepository.findByIdOrNull(studyId)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_NOT_FOUND)
    }

    private fun validateStudyLeader(study: Study, userId: UserId) {
        if (!study.isLeader(userId)) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }
    }
}
