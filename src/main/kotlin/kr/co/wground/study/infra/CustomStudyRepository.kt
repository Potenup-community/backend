package kr.co.wground.study.infra

import kr.co.wground.study.application.dto.StudySearchCondition
import kr.co.wground.study.domain.Study
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface CustomStudyRepository {
    fun searchStudies(condition: StudySearchCondition, pageable: Pageable): Slice<Study>
}