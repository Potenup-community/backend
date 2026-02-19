package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyReportDetailQueryResult
import kr.co.wground.study.application.dto.StudyReportSummaryQueryResult
import kr.co.wground.study.application.dto.StudyReportSearchCondition
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.StudyReport
import kr.co.wground.study.infra.StudyReportRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StudyReportQueryService(
    private val studyReportRepository: StudyReportRepository,
    private val userRepository: UserRepository,
) {

    fun getReportDetail(studyId: Long, userId: UserId): StudyReportDetailQueryResult {
        val report = findReportByStudyIdOrThrows(studyId)
        val requester = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val isAdmin = requester.role == UserRole.ADMIN
        if (!isAdmin && !report.study.isLeader(userId)) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }

        return StudyReportDetailQueryResult.of(report)
    }

    fun searchReports(userId: UserId, condition: StudyReportSearchCondition, pageable: Pageable): Slice<StudyReportSummaryQueryResult> {
        val requester = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (requester.role != UserRole.ADMIN) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }

        val reportPage = if (condition.status == null) {
            studyReportRepository.findAll(pageable)
        } else {
            studyReportRepository.findAllByStatus(condition.status, pageable)
        }

        val leaderIds = reportPage.content.map { it.study.leaderId }.distinct()
        val leadersById = userRepository.findByUserIdIn(leaderIds)
            .associateBy { it.userId }

        return reportPage.map { report ->
            val leader = leadersById[report.study.leaderId]
                ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
            StudyReportSummaryQueryResult.of(report, leader.name)
        }
    }

    // ----- helpers

    private fun findReportByStudyIdOrThrows(studyId: Long): StudyReport {
        return studyReportRepository.findByStudyId(studyId)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_REPORT_NOT_FOUND)
    }
}
