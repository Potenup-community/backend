package kr.co.wground.study.infra

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.study.application.dto.QStudyReportDetailQueryResult
import kr.co.wground.study.application.dto.QStudyReportSummaryQueryResult
import kr.co.wground.study.application.dto.StudyReportSummaryQueryResult
import kr.co.wground.study.application.dto.StudyReportSearchCondition
import kr.co.wground.study.domain.QStudy.study
import kr.co.wground.study.domain.QStudyReport.studyReport
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import kr.co.wground.user.domain.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class CustomStudyReportRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomStudyReportRepository {

    override fun findDetailByStudyId(studyId: Long) = queryFactory
        .select(
            QStudyReportDetailQueryResult(
                studyReport.id,
                study.id,
                study.leaderId,
                studyReport.status,
                studyReport.weeklyActivities.week1Activity,
                studyReport.weeklyActivities.week2Activity,
                studyReport.weeklyActivities.week3Activity,
                studyReport.weeklyActivities.week4Activity,
                studyReport.teamRetrospective.retrospectiveGood,
                studyReport.teamRetrospective.retrospectiveImprove,
                studyReport.teamRetrospective.retrospectiveNextAction,
                studyReport.submittedAt,
                studyReport.lastModifiedAt,
            )
        )
        .from(studyReport)
        .join(studyReport.study, study)
        .where(study.id.eq(studyId))
        .fetchOne()

    override fun searchSummaries(condition: StudyReportSearchCondition, pageable: Pageable): Page<StudyReportSummaryQueryResult> {
        val content = queryFactory
            .select(
                QStudyReportSummaryQueryResult(
                    studyReport.id,
                    study.id,
                    study.name,
                    study.leaderId,
                    user.name,
                    studyReport.status,
                    studyReport.submittedAt,
                    studyReport.lastModifiedAt,
                )
            )
            .from(studyReport)
            .join(studyReport.study, study)
            .join(user).on(study.leaderId.eq(user.userId))
            .where(statusEq(condition.status))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(*orderSpecifiers(pageable).toTypedArray())
            .fetch()

        val total = queryFactory
            .select(studyReport.count())
            .from(studyReport)
            .where(statusEq(condition.status))
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    private fun statusEq(status: StudyReportApprovalStatus?): BooleanExpression? {
        return status?.let { studyReport.status.eq(it) }
    }

    private fun orderSpecifiers(pageable: Pageable): List<OrderSpecifier<*>> {
        if (pageable.sort.isUnsorted) {
            return listOf(studyReport.submittedAt.desc())
        }

        val specifiers = mutableListOf<OrderSpecifier<*>>()
        pageable.sort.forEach { sortOrder ->
            when (sortOrder.property) {
                "submittedAt" -> specifiers += if (sortOrder.isAscending) studyReport.submittedAt.asc() else studyReport.submittedAt.desc()
                "lastModifiedAt" -> specifiers += if (sortOrder.isAscending) studyReport.lastModifiedAt.asc() else studyReport.lastModifiedAt.desc()
                "status" -> specifiers += if (sortOrder.isAscending) studyReport.status.asc() else studyReport.status.desc()
                "reportId", "id" -> specifiers += if (sortOrder.isAscending) studyReport.id.asc() else studyReport.id.desc()
            }
        }

        if (specifiers.isEmpty()) {
            specifiers += studyReport.submittedAt.desc()
        }

        return specifiers
    }
}
