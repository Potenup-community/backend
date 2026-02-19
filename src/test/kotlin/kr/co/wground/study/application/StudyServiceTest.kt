package kr.co.wground.study.application

import jakarta.persistence.EntityManager
import java.time.LocalDate
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.application.dto.StudyUpdateCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.WeeklyPlans
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study_schedule.domain.enums.Months
import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
@DisplayName("스터디 서비스 테스트")
class StudyServiceTest {
    @Autowired
    private lateinit var studyService: StudyService

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    private lateinit var studyScheduleRepository: StudyScheduleRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var studyRepository: StudyRepository

    @Autowired
    private lateinit var studyRecruitmentRepository: StudyRecruitmentRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    // ----- 생성 테스트

    /*
     * [참고]
     * - 특정 스터디에 참여 했다는 것은, 해당 스터디에 대한 승인된 신청 건이 존재함을 의미한다.
     * - 지금부터 스터디를 생성한 사람을 "스터디장"이라고 표현한다.
     * - 테스트에서 별도로 참여시키는 게 아니라, 스터디 생성 후 자동으로 참여된 상태임을 확인하는 테스트 임
     */
    @Test
    @DisplayName("스터디를 생성한 뒤, 반드시 해당 스터디에 참여된 상태여야 한다")
    fun shouldAutoEnrollLeader_whenCreateStudy() {

        /*
         * given
         * 1. ENROLLED 상태인 트랙
         * 2. 모집 기간이 종료되지 않은 스터디 일정
         * 3. ENROLLED 상태인 트랙에 참여 중인 사용자
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val user = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-1234-5678",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedUser = userRepository.save(user)

        // when: 스터디 생성 시
        val command = StudyCreateCommand(
            userId = savedUser.userId,
            name = "테스트 스터디",
            description = "스터디 설명",
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "🍕🍕🍕",
            chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
            refUrl = null,
            week1Plan = "1주차 계획",
            week2Plan = "2주차 계획",
            week3Plan = "3주차 계획",
            week4Plan = "4주차 계획",
            tags = emptyList(),
        )
        val studyId = studyService.createStudy(command)

        // then: 반드시 자신이 생성한 스터디에 참여된 상태여야 한다.
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(1, recruitments.size)

        val leaderRecruitment = recruitments.first()
        assertEquals(savedUser.userId, leaderRecruitment.userId)
        assertEquals(savedStudy?.id, leaderRecruitment.study.id)
    }

    /*
     * [참고]
     * - 지금부터 "졸업생"이란 종료된 트랙에 속한 사용자를 가리킨다.
     */
    @Test
    @DisplayName("졸업생이 스터디 생성을 시도한 경우, 예외 발생 - BusinessException(TRACK_IS_NOT_ENROLLED)")
    fun shouldThrowTrackIsNotEnrolled_whenGraduatedStudentCreatesStudy() {

        /*
         * given
         * 1. GRADUATED 상태인 트랙
         * 2. 종료된 트랙에 속한 사용자
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveGraduatedTrack(today)

        val user = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "졸업생",
            phoneNumber = "010-0000-0000",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedUser = userRepository.save(user)

        // when: 졸업생이 스터디 생성 시도
        // then: BusinessException 발생(TRACK_IS_NOT_ENROLLED)
        val thrown = assertThrows<BusinessException> {
            val command = StudyCreateCommand(
                userId = savedUser.userId,
                name = "졸업생 스터디",
                description = "졸업생 스터디 설명",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
            studyService.createStudy(command)
        }

        assertEquals(StudyServiceErrorCode.TRACK_IS_NOT_ENROLLED.code, thrown.code)
    }

    // ----- 태그 관련

    @Test
    @DisplayName("스터디 생성 시, 태그 개수가 MAX TAG COUNT 개를 초과한 경우 예외 발생 - BusinessException(STUDY_TAG_COUNT_EXCEEDED)")
    fun shouldThrowStudyTagCountExceeded_whenCreateStudyWithTooManyTags() {

        /*
         * given
         * 1. ENROLLED 상태인 트랙
         * 2. 모집 기간이 종료되지 않은 스터디 일정
         * 3. ENROLLED 상태인 트랙에 참여 중인 사용자
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val user = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "테스트 사용자",
            phoneNumber = "010-1111-1111",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedUser = userRepository.save(user)

        val tags = (1..(Study.MAX_TAG_COUNT + 1)).map { "tag$it" }

        // when: MAX_TAG_COUNT 초과 태그로 스터디 생성 시도
        // then: BusinessException 발생(STUDY_TAG_COUNT_EXCEEDED)
        val thrown = assertThrows<BusinessException> {
            val command = StudyCreateCommand(
                userId = savedUser.userId,
                name = "태그 초과 스터디",
                description = "태그 초과 생성 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = tags
            )
            studyService.createStudy(command)
        }

        assertEquals(StudyDomainErrorCode.STUDY_TAG_COUNT_EXCEEDED.code, thrown.code)
    }

    @Test
    @DisplayName("스터디 수정 시, 태그 개수가 MAX TAG COUNT 개를 초과한 경우 예외 발생 - BusinessException(STUDY_TAG_COUNT_EXCEEDED)")
    fun shouldThrowStudyTagCountExceeded_whenUpdateStudyWithTooManyTags() {

        /*
         * given
         * 1. ENROLLED 상태인 트랙
         * 2. 모집 기간이 종료되지 않은 스터디 일정
         * 3. 스터디 생성 완료 상태
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val user = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "테스트 사용자",
            phoneNumber = "010-2222-2222",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedUser = userRepository.save(user)

        val createCommand = StudyCreateCommand(
            userId = savedUser.userId,
            name = "태그 수정 스터디",
            description = "태그 수정 테스트",
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "🍕🍕🍕",
            chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
            refUrl = null,
            week1Plan = "1주차 계획",
            week2Plan = "2주차 계획",
            week3Plan = "3주차 계획",
            week4Plan = "4주차 계획",
            tags = listOf("tag1", "tag2")
        )
        val studyId = studyService.createStudy(createCommand)

        val updateTags = (1..(Study.MAX_TAG_COUNT + 1)).map { "tag$it" }

        // when: MAX_TAG_COUNT 초과 태그로 수정 시도
        // then: BusinessException 발생(STUDY_TAG_COUNT_EXCEEDED)
        val thrown = assertThrows<BusinessException> {
            val command = StudyUpdateCommand(
                studyId = studyId,
                userId = savedUser.userId,
                name = null,
                description = null,
                capacity = null,
                budget = null,
                budgetExplain = null,
                chatUrl = null,
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = updateTags
            )
            studyService.updateStudy(command)
        }

        assertEquals(StudyDomainErrorCode.STUDY_TAG_COUNT_EXCEEDED.code, thrown.code)
    }

    // ----- 결재 테스트

    // ----- 마감 테스트

    // ----- 참여 테스트

    // To Do: 모집 마김 일자가 지나기 전에 마감 시도한 경우 예외 발생 - BusinessException(RECRUITMENT_NOT_ENDED_YET)

    // ----- 삭제 테스트

    @Test
    @DisplayName("PENDING 상태의 스터디를 삭제한 경우, 관련된 모든 신청 건이 같이 삭제된다")
    fun shouldDeleteRecruitments_whenDeletePendingStudy() {

        /*
         * given
         * 1. PENDING 상태의 스터디
         * 2. 스터디장 + 추가 신청 건 존재
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val leader = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-5555-5555",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "삭제 테스트 스터디(PENDING)",
                description = "삭제 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val extraRecruitment = StudyRecruitment.apply(
            userId = 2L,
            study = savedStudy!!,
        )
        studyRecruitmentRepository.save(extraRecruitment)

        entityManager.flush()
        entityManager.clear()

        val recruitmentsBefore = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(2, recruitmentsBefore.size)

        // when: 스터디 삭제
        studyService.deleteStudy(studyId, savedLeader.userId, isAdmin = false)

        entityManager.flush()
        entityManager.clear()

        // then: 스터디 및 관련 신청건 삭제
        val deletedStudy = studyRepository.findByIdOrNull(studyId)
        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)

        assertEquals(null, deletedStudy)
        assertEquals(0, recruitments.size)
    }

    @Test
    @DisplayName("CLOSED 상태의 스터디를 삭제한 경우, 관련된 모든 신청 건이 같이 삭제된다")
    fun shouldDeleteRecruitments_whenDeleteClosedStudy() {

        /*
         * given
         * 1. CLOSED 상태의 스터디
         * 2. 스터디장 + 추가 신청 건 존재
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val leader = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-6666-6666",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "삭제 테스트 스터디(CLOSED)",
                description = "삭제 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val extraRecruitment = StudyRecruitment.apply(
            userId = 2L,
            study = savedStudy!!,
        )
        studyRecruitmentRepository.save(extraRecruitment)

        entityManager.flush()
        entityManager.clear()

        val recruitmentsBefore = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(2, recruitmentsBefore.size)

        // 모집 마감 + 최소 인원 충족 -> CLOSED
        val managedStudy = studyRepository.findByIdOrNull(studyId)!!
        managedStudy.close()

        // when: 스터디 삭제
        studyService.deleteStudy(studyId, savedLeader.userId, isAdmin = false)

        entityManager.flush()
        entityManager.clear()

        // then: 스터디 및 관련 신청건 삭제
        val deletedStudy = studyRepository.findByIdOrNull(studyId)
        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)

        assertEquals(null, deletedStudy)
        assertEquals(0, recruitments.size)
    }

    @Test
    @DisplayName("결재(APPROVED) 상태의 스터디를 삭제하려 한 경우, 예외 발생 - BusinessException(STUDY_CANT_DELETE_STATUS_DETERMINE)")
    fun shouldThrowStudyCantDeleteStatusDetermine_whenDeleteApprovedStudy() {

        /*
         * given
         * 1. APPROVED 상태의 스터디
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val leader = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-8888-8888",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        val student = User(
            trackId = savedTrack.trackId,
            email = "test2@gmail.com",
            name = "참가자",
            phoneNumber = "010-9999-9999",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedStudent = userRepository.save(student)

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "삭제 테스트 스터디(APPROVED)",
                description = "삭제 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        // 모집 마감 + 최소 인원 충족 -> CLOSED -> APPROVED
        savedStudy!!.participate(savedStudent.userId)
        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = today.minusDays(10),
            newRecruitEnd = today.minusDays(5),
            newStudyEnd = today.plusDays(10)
        )
        savedStudy.close()
        savedStudy.approve()
        assertEquals(StudyStatus.APPROVED, savedStudy.status)

        // when: APPROVED 스터디 삭제 시도
        val thrown = assertThrows<BusinessException> {
            studyService.deleteStudy(savedStudy.id, savedLeader.userId, isAdmin = false)
        }

        // then: 예외 발생(STUDY_CANT_DELETE_STATUS_DETERMINE)
        assertEquals(StudyDomainErrorCode.STUDY_CANT_DELETE_STATUS_APPROVED.code, thrown.code)
    }

    // ----- 참여 스터디 수 제한 테스트

    // To Do: 현재 차수 스터디에 2개 생성한 상태에서, 한 번 더 생성 시 예외 발생 - BusinessException

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 참여 중일 때, 신규 스터디 생성 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
    fun shouldThrowMaxStudyExceeded_whenTwoApprovedApplicationsExist() {

        /*
         * given
         * 1. ENROLLED 상태의 트랙
         * 2. 과거 차수 일정/스터디 참여 이력
         * 3. 현재 차수 스터디 2개에 APPROVED 신청
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val pastSchedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.FIRST,
            recruitStartDate = today.minusDays(40),
            recruitEndDate = today.minusDays(35),
            studyEndDate = today.minusDays(20)
        )
        val savedPastSchedule = studyScheduleRepository.save(pastSchedule)

        val currentSchedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.SECOND,
            recruitStartDate = today.minusDays(1),
            recruitEndDate = today.plusDays(3),
            studyEndDate = today.plusDays(15)
        )
        studyScheduleRepository.save(currentSchedule)

        val student = User(
            trackId = savedTrack.trackId,
            email = "student@gmail.com",
            name = "교육생",
            phoneNumber = "010-9999-9999",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedStudent = userRepository.save(student)

        // 과거 차수 참여 이력
        val pastStudy = Study.createNew(
            name = "과거 차수 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedPastSchedule.id,
            description = "과거 차수 참여",
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "🍕🍕🍕",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
        )
        val savedPastStudy = studyRepository.save(pastStudy)
        val pastRecruitment = StudyRecruitment.apply(
            userId = savedStudent.userId,
            study = savedPastStudy,
        )
        studyRecruitmentRepository.save(pastRecruitment)

        // 현재 차수 스터디 2개 생성(서로 다른 스터디)
        val leader1 = userRepository.save(
            User(
                trackId = savedTrack.trackId,
                email = "leader1@gmail.com",
                name = "리더1",
                phoneNumber = "010-1010-1010",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId1 = studyService.createStudy(
            StudyCreateCommand(
                userId = leader1.userId,
                name = "현재 차수 스터디1",
                description = "현재 차수 스터디1",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
        )

        val leader2 = userRepository.save(
            User(
                trackId = savedTrack.trackId,
                email = "leader2@gmail.com",
                name = "리더2",
                phoneNumber = "010-2020-2020",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId2 = studyService.createStudy(
            StudyCreateCommand(
                userId = leader2.userId,
                name = "현재 차수 스터디2",
                description = "현재 차수 스터디2",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
        )

        val currentStudy1 = studyRepository.findByIdOrNull(studyId1)!!
        val currentStudy2 = studyRepository.findByIdOrNull(studyId2)!!

        val approvedRecruitment1 = StudyRecruitment.apply(
            userId = savedStudent.userId,
            study = currentStudy1,
        )
        val approvedRecruitment2 = StudyRecruitment.apply(
            userId = savedStudent.userId,
            study = currentStudy2,
        )
        studyRecruitmentRepository.saveAll(listOf(approvedRecruitment1, approvedRecruitment2))

        entityManager.flush()
        entityManager.clear()

        // when: 신규 스터디 생성 시도
        val thrown = assertThrows<BusinessException> {
            val command = StudyCreateCommand(
                userId = savedStudent.userId,
                name = "추가 스터디",
                description = "추가 스터디",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
                tags = emptyList()
            )
            studyService.createStudy(command)
        }

        // then: 예외 발생(MAX_STUDY_EXCEEDED)
        assertEquals(StudyServiceErrorCode.MAX_STUDY_EXCEEDED.code, thrown.code)
    }

    // ----- helpers

    private fun createAndSaveEnrolledTrack(today: LocalDate): Track {
        val track = Track(
            trackName = "테스트 트랙",
            startDate = today.minusDays(30),
            endDate = today.plusDays(30)
        )
        return trackRepository.save(track)
    }

    private fun createAndSaveGraduatedTrack(today: LocalDate): Track {
        val track = Track(
            trackName = "졸업 트랙",
            startDate = today.minusDays(60),
            endDate = today.minusDays(1)
        )
        return trackRepository.save(track)
    }

    private fun createAndSaveCurrentSchedule(today: LocalDate, savedTrack: Track): StudySchedule {
        val schedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.FIRST,
            recruitStartDate = today.minusDays(1),
            recruitEndDate = today.plusDays(1),
            studyEndDate = today.plusDays(10)
        )
        return studyScheduleRepository.save(schedule)
    }
}
