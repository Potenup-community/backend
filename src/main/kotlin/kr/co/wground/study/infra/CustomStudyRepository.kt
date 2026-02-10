package kr.co.wground.study.infra

import kr.co.wground.common.SortType
import kr.co.wground.study.application.dto.StudyQueryResult
import kr.co.wground.study.application.dto.StudySearchCondition
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface CustomStudyRepository {
    fun searchStudies(condition: StudySearchCondition, pageable: Pageable, sortType: SortType): Slice<StudyQueryResult>
}