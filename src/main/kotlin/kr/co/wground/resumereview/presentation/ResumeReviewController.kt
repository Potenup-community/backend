package kr.co.wground.resumereview.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.resumereview.application.ResumeReviewService
import kr.co.wground.resumereview.presentation.request.CreateResumeReviewRequest
import kr.co.wground.resumereview.presentation.response.toResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/resume-reviews")
class ResumeReviewController(
    private val resumeReviewService: ResumeReviewService
): ResumeReviewApi {
    @PostMapping
    override fun reviewRequest(@RequestBody request: CreateResumeReviewRequest, userId: CurrentUserId) =
        ResponseEntity.status(HttpStatus.ACCEPTED).body(
            resumeReviewService.review(request.toDto(userId.value)).toResponse()
        )

    @GetMapping
    override fun getMyReviews(userId: CurrentUserId) =
        resumeReviewService.getMyReviews(userId.value).toResponse()

    @GetMapping("/{id}")
    override fun getMyReview(@PathVariable id: Long, userId: CurrentUserId) =
        resumeReviewService.getMyReview(id, userId.value).toResponse()
}
