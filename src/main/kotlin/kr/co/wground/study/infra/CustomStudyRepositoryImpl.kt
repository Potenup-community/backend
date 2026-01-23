package kr.co.wground.study.infra

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.study.application.dto.StudySearchCondition
import kr.co.wground.study.domain.QStudy.study
import kr.co.wground.study.domain.QStudyTag.studyTag
import kr.co.wground.study.domain.QTag.tag
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.constant.StudyStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class CustomStudyRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomStudyRepository {

    override fun searchStudies(condition: StudySearchCondition, pageable: Pageable): Slice<Study> {
        val pageSize = pageable.pageSize

        val content = queryFactory
            .selectFrom(study)
            .distinct()
            .leftJoin(study._studyTags, studyTag)
            .leftJoin(studyTag.tag, tag)
            .where(
                trackIdEq(condition.trackId),
                statusEq(condition.status),
            )
            .offset(pageable.offset)
            .limit(pageSize.toLong() + 1)
            .orderBy(*getOrderSpecifiers(pageable.sort))
            .fetch()

        return checkHasNext(pageSize, pageable, content)
    }

    private fun checkHasNext(pageSize: Int, pageable: Pageable, content: MutableList<Study>): Slice<Study> {
        var hasNext = false
        if (content.size > pageSize) {
            content.removeAt(pageSize)
            hasNext = true
        }
        return SliceImpl(content, pageable, hasNext)
    }

    private fun getOrderSpecifiers(sort: Sort): Array<OrderSpecifier<*>> {
        val orders = mutableListOf<OrderSpecifier<*>>()

        if (sort.isEmpty) {
            orders.add(OrderSpecifier(Order.DESC, study.createdAt))
            return orders.toTypedArray()
        }

        sort.forEach { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC

            val specifier = when (order.property) {
                "createdAt" -> OrderSpecifier(direction, study.createdAt)
                "name" -> OrderSpecifier(direction, study.name)
                "capacity" -> OrderSpecifier(direction, study.capacity)
                "budget" -> OrderSpecifier(direction, study.budget)
                else -> OrderSpecifier(Order.DESC, study.createdAt) // 매핑되지 않은 필드는 최신순 기본
            }
            orders.add(specifier)
        }

        return orders.toTypedArray()
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