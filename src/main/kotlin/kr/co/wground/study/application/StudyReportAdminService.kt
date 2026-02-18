package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyReportApprovalHistoryQueryResult
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.StudyReport
import kr.co.wground.study.domain.StudyReportApprovalHistory
import kr.co.wground.study.domain.enums.StudyReportApprovalAction
import kr.co.wground.study.infra.StudyReportApprovalHistoryRepository
import kr.co.wground.study.infra.StudyReportRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudyReportAdminService(
    private val studyReportRepository: StudyReportRepository,
    private val studyReportApprovalHistoryRepository: StudyReportApprovalHistoryRepository,
) {

    fun approve(studyId: Long, adminId: UserId) {
        val report = findReportByStudyIdOrThrows(studyId)
        report.markApproved()

        studyReportApprovalHistoryRepository.save(
            StudyReportApprovalHistory.create(
                studyReport = report,
                action = StudyReportApprovalAction.APPROVE,
                actorId = adminId,
            )
        )
    }

    fun reject(studyId: Long, adminId: UserId, reason: String) {
        val report = findReportByStudyIdOrThrows(studyId)
        report.markRejected(reason)

        studyReportApprovalHistoryRepository.save(
            StudyReportApprovalHistory.create(
                studyReport = report,
                action = StudyReportApprovalAction.REJECT,
                actorId = adminId,
                reason = reason,
            )
        )
    }

    fun cancel(studyId: Long, adminId: UserId, reason: String? = null) {
        val report = findReportByStudyIdOrThrows(studyId)
        report.cancelApprovalOrRejection()

        studyReportApprovalHistoryRepository.save(
            StudyReportApprovalHistory.create(
                studyReport = report,
                action = StudyReportApprovalAction.CANCEL,
                actorId = adminId,
                reason = reason,
            )
        )
    }

    @Transactional(readOnly = true)
    fun getApprovalHistories(studyId: Long): List<StudyReportApprovalHistoryQueryResult> {
        val report = findReportByStudyIdOrThrows(studyId)

        return studyReportApprovalHistoryRepository.findAllByStudyReportIdOrderByTimestampDesc(report.id)
            .map { StudyReportApprovalHistoryQueryResult.of(it) }
    }

    private fun findReportByStudyIdOrThrows(studyId: Long): StudyReport {
        return studyReportRepository.findByStudyId(studyId)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_REPORT_NOT_FOUND)
    }
}
