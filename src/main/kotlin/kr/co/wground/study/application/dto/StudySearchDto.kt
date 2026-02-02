package kr.co.wground.study.application.dto

import kr.co.wground.common.SortType
import kr.co.wground.global.common.UserId
import org.springframework.data.domain.Pageable

data class StudySearchDto(
    val userId: UserId,
    val condition: StudySearchCondition,
    val sortType: SortType,
    val pageable: Pageable
) {
    companion object {
        fun of(userId: UserId, condition: StudySearchCondition, sortType: SortType, pageable: Pageable): StudySearchDto {
            return StudySearchDto(
                userId = userId,
                condition = condition,
                sortType = sortType,
                pageable = pageable
            )
        }
    }
}
