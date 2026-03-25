package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyReportSearchCondition
import kr.co.wground.study.application.dto.StudyReportUpsertCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.WeeklyPlans
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
@DisplayName("스터디 결과 보고 조회 서비스 테스트")
class StudyReportQueryServiceTest {

    @Autowired
    private lateinit var studyReportService: StudyReportService

    @Autowired
    private lateinit var studyReportAdminService: StudyReportAdminService

    @Autowired
    private lateinit var studyReportQueryService: StudyReportQueryService

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    private lateinit var studyScheduleRepository: StudyScheduleRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var studyRepository: StudyRepository

    @Test
    @DisplayName("관리자는 스터디 결과 보고 상세를 조회할 수 있다")
    fun shouldGetReportDetail_whenRequesterIsAdmin() {
        val fixture = createFixtureWithTwoReports()

        val detail = studyReportQueryService.getReportDetail(fixture.firstStudy.id, fixture.admin.userId)

        assertEquals(fixture.firstStudy.id, detail.studyId)
        assertTrue(detail.week1Activity.isNotBlank())
    }

    @Test
    @DisplayName("스터디장은 자신의 스터디 결과 보고 상세를 조회할 수 있다")
    fun shouldGetReportDetail_whenRequesterIsLeader() {
        val fixture = createFixtureWithTwoReports()

        val detail = studyReportQueryService.getReportDetail(fixture.firstStudy.id, fixture.firstLeader.userId)

        assertEquals(fixture.firstStudy.id, detail.studyId)
    }

    @Test
    @DisplayName("관리자도 스터디장도 아닌 사용자가 상세 조회하면 예외 발생 - BusinessException(NOT_STUDY_LEADER)")
    fun shouldThrowNotStudyLeader_whenRequesterIsNotLeaderNorAdmin() {
        val fixture = createFixtureWithTwoReports()

        val thrown = assertThrows<BusinessException> {
            studyReportQueryService.getReportDetail(fixture.firstStudy.id, fixture.member.userId)
        }

        assertEquals(StudyServiceErrorCode.NOT_STUDY_LEADER_NOR_ADMIN.code, thrown.code)
    }

    @Test
    @DisplayName("결과 보고 목록 조회 시 상태 필터가 적용된다")
    fun shouldFilterByStatus_whenSearchReports() {
        val fixture = createFixtureWithTwoReports()
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "submittedAt"))

        val approvedReports = studyReportQueryService.searchReports(
            userId = fixture.admin.userId,
            condition = StudyReportSearchCondition(status = StudyReportApprovalStatus.APPROVED),
            pageable = pageable,
        )

        assertFalse(approvedReports.content.isEmpty())
        assertTrue(approvedReports.content.all { it.status == StudyReportApprovalStatus.APPROVED })
        assertEquals(listOf(fixture.secondStudy.id), approvedReports.content.map { it.studyId })
        assertEquals(1L, approvedReports.totalElements)
        assertEquals(1, approvedReports.totalPages)
    }

    @Test
    @DisplayName("관리자가 아닌 사용자가 결과 보고 목록 조회 시도하면 예외 발생 - BusinessException(NOT_STUDY_LEADER)")
    fun shouldThrowNotStudyLeader_whenNonAdminSearchesReports() {
        val fixture = createFixtureWithTwoReports()
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "submittedAt"))

        val thrown = assertThrows<BusinessException> {
            studyReportQueryService.searchReports(
                userId = fixture.member.userId,
                condition = StudyReportSearchCondition(status = null),
                pageable = pageable,
            )
        }

        assertEquals(StudyServiceErrorCode.NOT_ADMIN.code, thrown.code)
    }

    private fun createFixtureWithTwoReports(): QueryFixture {
        val today = LocalDate.now()

        val track = trackRepository.save(
            Track(
                trackName = "리포트 조회 테스트 트랙",
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

        val firstLeader = userRepository.save(
            User(
                trackId = track.trackId,
                email = "query-leader-1-${System.nanoTime()}@gmail.com",
                name = "리더1",
                phoneNumber = "010-3000-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )
        val secondLeader = userRepository.save(
            User(
                trackId = track.trackId,
                email = "query-leader-2-${System.nanoTime()}@gmail.com",
                name = "리더2",
                phoneNumber = "010-3000-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )
        val member = userRepository.save(
            User(
                trackId = track.trackId,
                email = "query-member-${System.nanoTime()}@gmail.com",
                name = "멤버",
                phoneNumber = "010-3000-0003",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
            )
        )
        val admin = userRepository.save(
            User(
                trackId = track.trackId,
                email = "query-admin-${System.nanoTime()}@gmail.com",
                name = "관리자",
                phoneNumber = "010-3000-0004",
                provider = "GOOGLE",
                role = UserRole.ADMIN,
                status = UserStatus.ACTIVE,
            )
        )

        val firstStudy = createInProgressStudy(
            scheduleId = schedule.id,
            trackId = track.trackId,
            leader = firstLeader,
            participantId = member.userId,
            name = "조회 테스트 스터디 1",
        )
        val secondStudy = createInProgressStudy(
            scheduleId = schedule.id,
            trackId = track.trackId,
            leader = secondLeader,
            participantId = member.userId,
            name = "조회 테스트 스터디 2",
        )

        studyReportService.upsertAndSubmit(createUpsertCommand(firstStudy.id, firstLeader.userId, "첫번째"))

        studyReportService.upsertAndSubmit(createUpsertCommand(secondStudy.id, secondLeader.userId, "두번째"))
        studyReportAdminService.approve(secondStudy.id, admin.userId)

        return QueryFixture(
            firstLeader = firstLeader,
            secondLeader = secondLeader,
            member = member,
            admin = admin,
            firstStudy = firstStudy,
            secondStudy = secondStudy,
        )
    }

    private fun createInProgressStudy(
        scheduleId: Long,
        trackId: Long,
        leader: User,
        participantId: Long,
        name: String,
    ): Study {
        val study = Study.createNew(
            name = name,
            leaderId = leader.userId,
            trackId = trackId,
            scheduleId = scheduleId,
            description = "$name 설명",
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

        study.participate(participantId)
        study.closeRecruitment()
        study.start()
        return studyRepository.save(study)
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

    private data class QueryFixture(
        val firstLeader: User,
        val secondLeader: User,
        val member: User,
        val admin: User,
        val firstStudy: Study,
        val secondStudy: Study,
    )
}
