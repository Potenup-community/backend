package kr.co.wground.study.infra

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.common.SortType
import kr.co.wground.study.application.dto.QStudyQueryDto
import kr.co.wground.study.application.dto.StudyQueryResult
import kr.co.wground.study.application.dto.StudySearchCondition
import kr.co.wground.study.domain.QStudy.study
import kr.co.wground.study_schedule.domain.QStudySchedule.studySchedule
import kr.co.wground.study.domain.QTag.tag
import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.track.domain.QTrack.track
import kr.co.wground.user.domain.QUser.user
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class CustomStudyRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomStudyRepository {

    override fun searchStudies(condition: StudySearchCondition, pageable: Pageable, sortType: SortType): Slice<StudyQueryResult> {
        val pageSize = pageable.pageSize

        val content = queryFactory
            .select(QStudyQueryDto(study, studySchedule, user, track))
            .from(study)
            .join(studySchedule).on(study.scheduleId.eq(studySchedule.id))
            .join(user).on(study.leaderId.eq(user.userId))
            .join(track).on(user.trackId.eq(track.trackId))
            .where(
                trackIdEq(condition.trackId),
                statusEq(condition.status),
            )
            .offset(pageable.offset)
            .limit(pageSize.toLong() + 1)
            .orderBy(sortType.getOrderSpecifier())
            .fetch()

        var hasNext = false
        if (content.size > pageSize) {
            content.removeAt(pageSize)
            hasNext = true
        }

        return SliceImpl(content, pageable, hasNext)
    }

    private fun trackIdEq(trackId: Long?): BooleanExpression? {
        return trackId?.let { study.trackId.eq(it) }
    }

    private fun statusEq(status: StudyStatus?): BooleanExpression? {
        return status?.let { study.status.eq(it) }
    }

    private fun tagNamesIn(tagNames: List<String>?): BooleanExpression? {
        return if (tagNames.isNullOrEmpty()) null else tag.name.`in`(tagNames)
    }
}