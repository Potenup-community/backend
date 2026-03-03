package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.enums.StudyReportApprovalAction
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import lombok.AccessLevel
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@Table(
    name = "study_report_approval_history",
    indexes = [
        Index(
            name = "idx_study_report_approval_history_report_id_timestamp",
            columnList = "study_report_id, timestamp",
        ),
    ],
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StudyReportApprovalHistory private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_report_id", nullable = false)
    val studyReport: StudyReport,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    val action: StudyReportApprovalAction,

    @Column(nullable = false)
    val actorId: UserId,

    @Column(length = MAX_REASON_LENGTH)
    val reason: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        const val MAX_REASON_LENGTH = 1000

        fun create(
            studyReport: StudyReport,
            action: StudyReportApprovalAction,
            actorId: UserId,
            reason: String? = null,
            timestamp: LocalDateTime = LocalDateTime.now(),
        ): StudyReportApprovalHistory {
            val normalizedReason = reason?.trim()
            val isReasonNullOrEmpty = normalizedReason == null || normalizedReason.isEmpty()

            if (action == StudyReportApprovalAction.REJECT && isReasonNullOrEmpty) {
                throw BusinessException(StudyDomainErrorCode.STUDY_REPORT_REJECT_REASON_REQUIRED)
            }

            if (normalizedReason != null && normalizedReason.length > MAX_REASON_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.STUDY_REPORT_REASON_TOO_LONG)
            }

            return StudyReportApprovalHistory(
                studyReport = studyReport,
                action = action,
                actorId = actorId,
                reason = normalizedReason,
                timestamp = timestamp,
            )
        }
    }
}
