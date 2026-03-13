package kr.co.wground.resumereview.application

import kr.co.wground.common.outbox.repository.OutboxEventRepository
import kr.co.wground.common.outbox.ResumeReviewOutboxFactory
import kr.co.wground.exception.BusinessException
import kr.co.wground.resumereview.application.command.ResumeReviewCommand
import kr.co.wground.resumereview.application.command.dto.CreateResumeReviewDto
import kr.co.wground.resumereview.application.command.dto.ReviewAcceptedResultDto
import kr.co.wground.resumereview.application.command.dto.toDomain
import kr.co.wground.resumereview.application.hash.RequestHashCalculator
import kr.co.wground.common.event.ReviewRequestedEvent
import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.application.query.ResumeReviewQuery
import kr.co.wground.resumereview.application.query.dto.ResumeReviewDetailResultDto
import kr.co.wground.resumereview.application.query.dto.ResumeReviewResultDto
import kr.co.wground.resumereview.application.query.dto.toDto
import kr.co.wground.resumereview.domain.ResumeReview
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode
import kr.co.wground.resumereview.infra.ResumeReviewRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResumeReviewService(
    private val resumeReviewRepository: ResumeReviewRepository,
    private val requestHashCalculator: RequestHashCalculator,
    private val resumeReviewOutboxFactory: ResumeReviewOutboxFactory,
    private val outboxEventRepository: OutboxEventRepository,
) : ResumeReviewCommand, ResumeReviewQuery {
    override fun review(dto: CreateResumeReviewDto): ReviewAcceptedResultDto {
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

    @Transactional(readOnly = true)
    override fun getMyReviews(userId: UserId): List<ResumeReviewResultDto> {
        val foundEntities = resumeReviewRepository.findByUserId(userId)

        return foundEntities.toDto()
    }

    @Transactional(readOnly = true)
    override fun getMyReview(id: Long, userId: UserId): ResumeReviewDetailResultDto {
        val foundEntity = resumeReviewRepository.findByIdOrNull(id)
            ?: throw BusinessException(ResumeReviewErrorCode.NOT_FOUND_EXCEPTION)

        if (foundEntity.userId != userId) throw BusinessException(ResumeReviewErrorCode.NOT_OWNED_RESUME_RESULT)

        return foundEntity.toDto()
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
