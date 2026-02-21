package kr.co.wground.resumereview.application

import jakarta.transaction.Transactional
import kr.co.wground.common.outbox.OutboxEventRepository
import kr.co.wground.common.outbox.ResumeReviewOutboxFactory
import kr.co.wground.exception.BusinessException
import kr.co.wground.resumereview.application.command.ResumeReviewCommand
import kr.co.wground.resumereview.application.command.dto.CreateResumeReviewDto
import kr.co.wground.resumereview.application.command.dto.ReviewAcceptedResultDto
import kr.co.wground.resumereview.application.command.dto.toDomain
import kr.co.wground.resumereview.application.hash.RequestHashCalculator
import kr.co.wground.resumereview.application.publisher.ReviewRequestedEvent
import kr.co.wground.resumereview.application.query.ResumeReviewQuery
import kr.co.wground.resumereview.domain.ResumeReview
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode
import kr.co.wground.resumereview.infra.ResumeReviewRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
@Transactional
class ResumeReviewService(
    private val resumeReviewRepository: ResumeReviewRepository,
    private val requestHashCalculator: RequestHashCalculator,
    private val resumeReviewOutboxFactory: ResumeReviewOutboxFactory,
    private val outboxEventRepository: OutboxEventRepository,
) : ResumeReviewCommand, ResumeReviewQuery {
    fun review(dto: CreateResumeReviewDto): ReviewAcceptedResultDto {
        val hashedRequest = requestHashCalculator.calculate(dto)

        resumeReviewRepository.findByHash(hashedRequest)?.let {
            return ReviewAcceptedResultDto(resumeReviewId = it.id, status = it.status)
        }
        val resumeReview = dto.toDomain(hashedRequest)

        val savedEntity = try {
            val savedEntity = resumeReviewRepository.save(resumeReview)
            createOutbox(savedEntity, hashedRequest)
            savedEntity
        } catch (e: DataIntegrityViolationException) {
            resumeReviewRepository.findByHash(hashedRequest)
                ?: throw BusinessException(
                    errorCode = ResumeReviewErrorCode.UNEXPECTED_UNIQUE_VIOLATION_ERROR,
                    cause = e
                )
        }

        return ReviewAcceptedResultDto(resumeReviewId = savedEntity.id, status = savedEntity.status)
    }

    private fun createOutbox(savedEntity: ResumeReview, hashedRequest: String) {
        val outbox = resumeReviewOutboxFactory.requested(
            resumeReviewId = savedEntity.id,
            hash = hashedRequest,
            payload = ReviewRequestedEvent.of(
                resumeReviewId = savedEntity.id,
                userId = savedEntity.userId,
                title = savedEntity.resumeReviewTitle,
                jdUrl = savedEntity.jdUrl,
                section = savedEntity.resumeSections,
            )
        )

        outboxEventRepository.save(outbox)
    }
}
