package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyReportUpsertCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.WeeklyPlans
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study.domain.enums.StudyReportApprovalAction
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import kr.co.wground.study.infra.StudyReportApprovalHistoryRepository
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
@DisplayName("스터디 결과 보고 관리자 서비스 테스트")
class StudyReportAdminServiceTest {

    @Autowired
    private lateinit var studyReportService: StudyReportService

    @Autowired
    private lateinit var studyReportAdminService: StudyReportAdminService

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

    @Autowired
    private lateinit var studyReportApprovalHistoryRepository: StudyReportApprovalHistoryRepository

    @Test
    @DisplayName("관리자가 승인하면 보고서 상태는 APPROVED 가 되고 승인 이력이 저장된다")
    fun shouldApproveReportAndSaveHistory_whenAdminApproves() {
        val fixture = createSubmittedReportFixture()

        studyReportAdminService.approve(fixture.study.id, fixture.admin.userId)

        val report = studyReportRepository.findByStudyId(fixture.study.id)
        assertNotNull(report)
        assertEquals(StudyReportApprovalStatus.APPROVED, report?.status)

        val histories = studyReportApprovalHistoryRepository.findAllByStudyReportIdOrderByTimestampDesc(report!!.id)
        assertFalse(histories.isEmpty())
        assertEquals(StudyReportApprovalAction.APPROVE, histories.first().action)
        assertEquals(fixture.admin.userId, histories.first().actorId)
    }

    @Test
    @DisplayName("관리자가 반려하면 보고서 상태는 REJECTED 가 되고 반려 사유가 이력에 저장된다")
    fun shouldRejectReportAndSaveReason_whenAdminRejects() {
        val fixture = createSubmittedReportFixture()
        val reason = "주차별 증빙 내용이 부족합니다."

        studyReportAdminService.reject(fixture.study.id, fixture.admin.userId, reason)

        val report = studyReportRepository.findByStudyId(fixture.study.id)
        assertNotNull(report)
        assertEquals(StudyReportApprovalStatus.REJECTED, report?.status)

        val histories = studyReportApprovalHistoryRepository.findAllByStudyReportIdOrderByTimestampDesc(report!!.id)
        assertFalse(histories.isEmpty())
        assertEquals(StudyReportApprovalAction.REJECT, histories.first().action)
        assertEquals(reason, histories.first().reason)
    }

    @Test
    @DisplayName("승인 또는 반려를 취소하면 보고서 상태는 SUBMITTED 로 되돌아간다")
    fun shouldRevertToSubmitted_whenAdminCancelsDecision() {
        val fixture = createSubmittedReportFixture()
        val cancelReason = "오승인으로 인한 취소"

        studyReportAdminService.approve(fixture.study.id, fixture.admin.userId)
        studyReportAdminService.cancel(fixture.study.id, fixture.admin.userId, cancelReason)

        val report = studyReportRepository.findByStudyId(fixture.study.id)
        assertNotNull(report)
        assertEquals(StudyReportApprovalStatus.SUBMITTED, report?.status)

        val histories = studyReportApprovalHistoryRepository.findAllByStudyReportIdOrderByTimestampDesc(report!!.id)
        assertTrue(histories.any { it.action == StudyReportApprovalAction.CANCEL && it.reason == cancelReason })
    }

    @Test
    @DisplayName("결과 보고가 없는 스터디를 관리자가 결재하면 예외 발생 - BusinessException(STUDY_REPORT_NOT_FOUND)")
    fun shouldThrowStudyReportNotFound_whenAdminApprovesWithoutReport() {
        val fixture = createInProgressStudyFixture()

        val thrown = assertThrows<BusinessException> {
            studyReportAdminService.approve(fixture.study.id, fixture.admin.userId)
        }

        assertEquals(StudyServiceErrorCode.STUDY_REPORT_NOT_FOUND.code, thrown.code)
    }

    // ----- fixture

    private fun createSubmittedReportFixture(): ReportFixture {
        val fixture = createInProgressStudyFixture()
        val reportId = studyReportService.upsertAndSubmit(
            createUpsertCommand(
                studyId = fixture.study.id,
                userId = fixture.leader.userId,
                suffix = "초기",
            )
        )
        val report = studyReportRepository.findByIdOrNull(reportId)
            ?: throw IllegalStateException("결과 보고가 생성되지 않았습니다.")

        return ReportFixture(
            leader = fixture.leader,
            admin = fixture.admin,
            study = fixture.study,
            reportId = report.id,
        )
    }

    private fun createInProgressStudyFixture(): StudyFixture {
        val today = LocalDate.now()

        val track = trackRepository.save(
            Track(
                trackName = "리포트 관리자 테스트 트랙",
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
                email = "admin-report-leader-${System.nanoTime()}@gmail.com",
                name = "리더",
                phoneNumber = "010-2000-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )

        val member = userRepository.save(
            User(
                trackId = track.trackId,
                email = "admin-report-member-${System.nanoTime()}@gmail.com",
                name = "멤버",
                phoneNumber = "010-2000-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )

        val admin = userRepository.save(
            User(
                trackId = track.trackId,
                email = "admin-report-admin-${System.nanoTime()}@gmail.com",
                name = "관리자",
                phoneNumber = "010-2000-0003",
                provider = "GOOGLE",
                role = UserRole.ADMIN,
                status = UserStatus.ACTIVE,
            )
        )

        val study = Study.createNew(
            name = "리포트 관리자 테스트 스터디",
            leaderId = leader.userId,
            trackId = track.trackId,
            scheduleId = schedule.id,
            description = "리포트 관리자 테스트 설명",
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
            admin = admin,
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
        val admin: User,
        val study: Study,
    )

    private data class ReportFixture(
        val leader: User,
        val admin: User,
        val study: Study,
        val reportId: Long,
    )
}
