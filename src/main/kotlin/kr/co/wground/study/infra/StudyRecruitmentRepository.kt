package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudyRecruitment
import org.springframework.data.jpa.repository.JpaRepository

interface StudyRecruitmentRepository : JpaRepository<StudyRecruitment, Long> {
}