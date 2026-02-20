package kr.co.wground.study.infra

import kr.co.wground.study.application.dto.StudyReportDetailQueryResult
import kr.co.wground.study.application.dto.StudyReportSearchCondition
import kr.co.wground.study.application.dto.StudyReportSummaryQueryResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomStudyReportRepository {
    fun findDetailByStudyId(studyId: Long): StudyReportDetailQueryResult?
    fun searchSummaries(condition: StudyReportSearchCondition, pageable: Pageable): Page<StudyReportSummaryQueryResult>
}
