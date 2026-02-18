package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import lombok.AccessLevel
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StudyReport private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    study: Study,

    weeklyActivities: WeeklyActivities,

    teamRetrospective: TeamRetrospective,

    status: StudyReportApprovalStatus = StudyReportApprovalStatus.SUBMITTED,

    submittedAt: LocalDateTime = LocalDateTime.now(),

    lastModifiedAt: LocalDateTime = LocalDateTime.now(),
) {

    /*
    * 구현 관련 메모입니다. 다른 데다 적으면 까먹을 거 같아서 일단 코드에 적어둘게요
    * OneToOne 으로 하지 않고 ManyToOne 으로 설정한 가장 큰 이유
    * - 현재 study_id 에 대해 유일키 제약조건 설정할 생각이라 스터디 당 결과 보고서 한 건만 존재할 수 있다는 제약조건 충족 가능
    * - 나중에 여러 건의 보고 건으로 확장 가능 성 용이함. 그냥 unique 풀어버리면 됨 예를 들면,
    *   - 현재는 반려 건에 대해 재상신 해버릴 수 있는데, 나중에 반려 건에 대한 결과 보고를 유지해야 하고, 무조건 새로 올려야 하도록
    *     정책이 바뀔 수도 있음
    *   - 보고 유형이 다양해질 수도 있음. 지금은 결과 보고 하나 뿐이지만, 나중에 소프트웨어 구독료 등의 지원 증빙에 대한 보고가 필요해 질 수도 있다고 생각했음
    * 암튼 위와 같이 보고 건이 여럿 존재하도록 변경될 가능성을 고려했음.
    *
    * ps. 이건 더 확실하게 알아봐야 하긴 하는데, Study 정보를 lazy 하게 가져오고 싶은데 OneToOne 으로 하면 뭔가 더 eager 하게 가져오려는
    * 경향이 있을 것 같아서 ManyToOne 으로 한 것도 있기는 합니다.
    * */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false, unique = true)
    val study: Study = study

    @Embedded
    var weeklyActivities: WeeklyActivities = weeklyActivities
        protected set

    @Embedded
    var teamRetrospective: TeamRetrospective = teamRetrospective
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StudyReportApprovalStatus = status
        protected set

    @Column(nullable = false)
    var submittedAt: LocalDateTime = submittedAt
        protected set

    @Column(nullable = false)
    var lastModifiedAt: LocalDateTime = lastModifiedAt
        protected set

    companion object {
        fun create(
            study: Study,
            weeklyActivities: WeeklyActivities,
            teamRetrospective: TeamRetrospective,
            now: LocalDateTime = LocalDateTime.now(),
        ): StudyReport {
            return StudyReport(
                study = study,
                weeklyActivities = weeklyActivities,
                teamRetrospective = teamRetrospective,
                status = StudyReportApprovalStatus.SUBMITTED,
                submittedAt = now,
                lastModifiedAt = now,
            )
        }
    }

    fun revise(
        weeklyActivities: WeeklyActivities,
        teamRetrospective: TeamRetrospective,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        this.weeklyActivities = weeklyActivities
        this.teamRetrospective = teamRetrospective
        this.lastModifiedAt = now
    }

    fun markSubmitted(now: LocalDateTime = LocalDateTime.now()) {
        this.status = StudyReportApprovalStatus.SUBMITTED
        this.submittedAt = now
        this.lastModifiedAt = now
    }

    fun markResubmitted(now: LocalDateTime = LocalDateTime.now()) {
        this.status = StudyReportApprovalStatus.RESUBMITTED
        this.submittedAt = now
        this.lastModifiedAt = now
    }

    fun markApproved(now: LocalDateTime = LocalDateTime.now()) {
        this.status = StudyReportApprovalStatus.APPROVED
        this.lastModifiedAt = now
    }

    fun markRejected(now: LocalDateTime = LocalDateTime.now()) {
        this.status = StudyReportApprovalStatus.REJECTED
        this.lastModifiedAt = now
    }

    fun cancelApproval(now: LocalDateTime = LocalDateTime.now()) {
        this.status = StudyReportApprovalStatus.SUBMITTED
        this.lastModifiedAt = now
    }
}
