package kr.co.wground.study.infra

import kr.co.wground.study.domain.Study
import org.springframework.data.jpa.repository.JpaRepository

interface StudyRepository : JpaRepository<Study, Long>{
}