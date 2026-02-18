package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.enums.StudyReportApprovalAction
import lombok.AccessLevel
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StudyReportApprovalHistory private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_report_id", nullable = false)
    val studyReport: StudyReport,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val action: StudyReportApprovalAction,

    @Column(nullable = false)
    val actorId: UserId,

    @Column(length = MAX_REASON_LENGTH)
    val reason: String? = null,

    @Column(nullable = false)
    val actedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        const val MAX_REASON_LENGTH = 1000

        fun create(
            studyReport: StudyReport,
            action: StudyReportApprovalAction,
            actorId: UserId,
            reason: String? = null,
            actedAt: LocalDateTime = LocalDateTime.now(),
        ): StudyReportApprovalHistory {
            
            // To Do: 현재 반려 건에 대해 reason 이 필수라는 점이 반영되어 있지 않음
            
            return StudyReportApprovalHistory(
                studyReport = studyReport,
                action = action,
                actorId = actorId,
                reason = reason?.trim(),
                actedAt = actedAt,
            )
        }
    }
}
