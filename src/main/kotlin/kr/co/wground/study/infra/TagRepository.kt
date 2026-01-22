package kr.co.wground.study.infra

import kr.co.wground.study.domain.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository: JpaRepository<Tag, Long> {
    fun findByName(name: String): Tag?
}