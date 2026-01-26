package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudyTag
import org.springframework.data.jpa.repository.JpaRepository

interface StudyTagRepository : JpaRepository<StudyTag, Long> {
}