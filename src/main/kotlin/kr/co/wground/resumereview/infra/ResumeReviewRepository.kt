package kr.co.wground.resumereview.infra

import kr.co.wground.resumereview.domain.ResumeReview
import org.springframework.data.jpa.repository.JpaRepository

interface ResumeReviewRepository: JpaRepository<ResumeReview, Long> {
    fun findByHash(hash: String): ResumeReview?
}
