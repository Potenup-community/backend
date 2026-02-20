package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyReportUpsertCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.WeeklyPlans
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import kr.co.wground.study.infra.StudyReportRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study_schedule.domain.enums.Months
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
@DisplayName("스터디 결과 보고 서비스 테스트")
class StudyReportServiceTest {

    @Autowired
    private lateinit var studyReportService: StudyReportService

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    private lateinit var studyScheduleRepository: StudyScheduleRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var studyRepository: StudyRepository

    @Autowired
    private lateinit var studyReportRepository: StudyReportRepository

    @Test
    @DisplayName("스터디장이 최초 상신하면 결과 보고가 생성되고 상태는 SUBMITTED 이다")
    fun shouldCreateSubmittedReport_whenLeaderSubmitsFirstTime() {
        val fixture = createInProgressStudyFixture()

        val reportId = studyReportService.upsertAndSubmit(
            createUpsertCommand(
                studyId = fixture.study.id,
                userId = fixture.leader.userId,
                suffix = "초기",
            )
        )

        val report = studyReportRepository.findByIdOrNull(reportId)
        assertNotNull(report)
        assertEquals(StudyReportApprovalStatus.SUBMITTED, report?.status)

        val submissionStatus = studyReportService.getMySubmissionStatus(fixture.study.id, fixture.leader.userId)
        assertTrue(submissionStatus.hasReport)
        assertEquals(StudyReportApprovalStatus.SUBMITTED, submissionStatus.status)
    }

    @Test
    @DisplayName("반려된 결과 보고를 수정 상신하면 상태는 RESUBMITTED 로 변경된다")
    fun shouldChangeStatusToResubmitted_whenRejectedReportUpdated() {
        val fixture = createInProgressStudyFixture()

        val firstReportId = studyReportService.upsertAndSubmit(
            createUpsertCommand(
                studyId = fixture.study.id,
                userId = fixture.leader.userId,
                suffix = "초기",
            )
        )
        val report = studyReportRepository.findByIdOrNull(firstReportId)!!
        report.markRejected("보완이 필요합니다.")

        val resubmittedReportId = studyReportService.upsertAndSubmit(
            createUpsertCommand(
                studyId = fixture.study.id,
                userId = fixture.leader.userId,
                suffix = "재상신",
            )
        )

        val updatedReport = studyReportRepository.findByIdOrNull(resubmittedReportId)
        assertEquals(firstReportId, resubmittedReportId)
        assertEquals(StudyReportApprovalStatus.RESUBMITTED, updatedReport?.status)
    }

    @Test
    @DisplayName("스터디장이 아닌 사용자가 상신 시도하면 예외 발생 - BusinessException(NOT_STUDY_LEADER)")
    fun shouldThrowNotStudyLeader_whenNonLeaderSubmitsReport() {
        val fixture = createInProgressStudyFixture()

        val thrown = assertThrows<BusinessException> {
            studyReportService.upsertAndSubmit(
                createUpsertCommand(
                    studyId = fixture.study.id,
                    userId = fixture.member.userId,
                    suffix = "권한없음",
                )
            )
        }

        assertEquals(StudyServiceErrorCode.NOT_STUDY_LEADER.code, thrown.code)
    }

    @Test
    @DisplayName("아직 결과 보고가 없는 경우 상신 상태 조회 시 hasReport 는 false 이다")
    fun shouldReturnNoReportStatus_whenReportNotExists() {
        val fixture = createInProgressStudyFixture()

        val submissionStatus = studyReportService.getMySubmissionStatus(fixture.study.id, fixture.leader.userId)

        assertFalse(submissionStatus.hasReport)
        assertEquals(null, submissionStatus.status)
        assertEquals(null, submissionStatus.submittedAt)
        assertEquals(null, submissionStatus.lastModifiedAt)
    }

    // ----- fixture

    private fun createInProgressStudyFixture(): StudyFixture {
        val today = LocalDate.now()

        val track = trackRepository.save(
            Track(
                trackName = "리포트 테스트 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30),
            )
        )

        val schedule = studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10),
            )
        )

        val leader = userRepository.save(
            User(
                trackId = track.trackId,
                email = "report-leader-${System.nanoTime()}@gmail.com",
                name = "리더",
                phoneNumber = "010-1000-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )

        val member = userRepository.save(
            User(
                trackId = track.trackId,
                email = "report-member-${System.nanoTime()}@gmail.com",
                name = "멤버",
                phoneNumber = "010-1000-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )

        val study = Study.createNew(
            name = "리포트 테스트 스터디",
            leaderId = leader.userId,
            trackId = track.trackId,
            scheduleId = schedule.id,
            description = "리포트 테스트 설명",
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "피자 먹기",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            externalChatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
            referenceUrl = null,
        )
        study.participate(member.userId)
        study.closeRecruitment()
        study.start()

        val savedStudy = studyRepository.save(study)

        return StudyFixture(
            leader = leader,
            member = member,
            study = savedStudy,
        )
    }

    private fun createUpsertCommand(studyId: Long, userId: Long, suffix: String): StudyReportUpsertCommand {
        return StudyReportUpsertCommand(
            studyId = studyId,
            userId = userId,
            week1Activity = "1주차 활동 $suffix",
            week2Activity = "2주차 활동 $suffix",
            week3Activity = "3주차 활동 $suffix",
            week4Activity = "4주차 활동 $suffix",
            retrospectiveGood = "잘한 점 $suffix",
            retrospectiveImprove = "개선할 점 $suffix",
            retrospectiveNextAction = "다음 액션 $suffix",
        )
    }

    private data class StudyFixture(
        val leader: User,
        val member: User,
        val study: Study,
    )
}
