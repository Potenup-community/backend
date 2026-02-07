package kr.co.wground.study.application

import jakarta.persistence.EntityManager
import java.time.LocalDate
import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.Months
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
@DisplayName("스터디 신청 서비스 테스트")
class StudyRecruitmentServiceTest {
    @Autowired
    private lateinit var studyService: StudyService

    @Autowired
    private lateinit var studyRecruitmentService: StudyRecruitmentService

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    private lateinit var studyScheduleRepository: StudyScheduleRepository

    @Autowired
    private lateinit var studyRepository: StudyRepository

    @Autowired
    private lateinit var studyRecruitmentRepository: StudyRecruitmentRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    companion object {
        @JvmStatic
        fun studyStatusCannotBeApplied(): Stream<Arguments> = Stream.of(
            Arguments.of("CLOSED", StudyStatus.CLOSED, StudyServiceErrorCode.STUDY_NOT_RECRUITING.code),
            Arguments.of("APPROVED", StudyStatus.APPROVED, StudyServiceErrorCode.STUDY_NOT_RECRUITING.code),
        )

        @JvmStatic
        fun studyStatusCannotBeWithdrawn(): Stream<Arguments> = Stream.of(
            Arguments.of("CLOSED", StudyStatus.CLOSED, StudyServiceErrorCode.RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_PENDING.code),
            Arguments.of("APPROVED", StudyStatus.APPROVED, StudyServiceErrorCode.RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_PENDING.code),
        )
    }

    // ----- 신청 테스트

    // To Do: 조기 수료 가능한 지 아름님한테 물어볼 것
    @Test
    @DisplayName("수료생이 스터디에 신청한 경우, 예외 발생 - BusinessException(GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY)")
    fun shouldThrowGraduatedStudentCantRecruitOfficialStudy_whenGraduatedStudentAppliesToStudy() {

        /*
         * given
         * 1. GRADUATED 상태의 트랙
         * 2. 종료된 스터디 일정 및 해당 스터디
         * 3. 종료된 트랙에 속한 사용자
         */
        val today = LocalDate.now()
        val track = Track(
            trackName = "졸업 트랙",
            startDate = today.minusDays(60),
            endDate = today.minusDays(1)
        )
        val savedTrack = trackRepository.save(track)

        val schedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.SIXTH,
            recruitStartDate = today.minusDays(1),
            recruitEndDate = today.plusDays(1),
            studyEndDate = today.plusDays(10)
        )
        val savedSchedule = studyScheduleRepository.save(schedule)

        val study = Study(
            name = "졸업 트랙 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedSchedule.id,
            description = "졸업 트랙 스터디 설명",
            status = StudyStatus.PENDING,
            capacity = 5,
            budget = BudgetType.MEAL
        )
        val savedStudy = studyRepository.save(study)

        val user = User(
            trackId = savedTrack.trackId,
            email = "graduate@gmail.com",
            name = "수료생",
            phoneNumber = "010-0000-0000",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedUser = userRepository.save(user)

        // when: 수료생이 스터디 신청 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = savedUser.userId,
                studyId = savedStudy.id,
            )
        }

        // then: 예외 발생(TRACK_IS_NOT_ENROLLED)
        assertEquals(StudyServiceErrorCode.GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 자신의 트랙이 아닌 스터디에 신청한 경우, 예외 발생 - BusinessException(TRACK_MISMATCH)")
    fun shouldThrowTrackMismatch_whenApplicantTrackDiffers() {

        /*
         * given
         * 1. 서로 다른 ENROLLED 트랙
         * 2. 사용자 트랙의 현재 차수 일정
         * 3. 다른 트랙의 모집 중 스터디
         * 4. 사용자
         */
        val today = LocalDate.now()
        val userTrack = trackRepository.save(
            Track(
                trackName = "사용자 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )
        val otherTrack = trackRepository.save(
            Track(
                trackName = "스터디 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )

        studyScheduleRepository.save(
            StudySchedule(
                trackId = userTrack.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )
        val otherTrackStudySchedule = studyScheduleRepository.save(
            StudySchedule(
                trackId = otherTrack.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )

        val study = studyRepository.save(
            Study(
                name = "타 트랙 스터디",
                leaderId = 10L,
                trackId = otherTrack.trackId,
                scheduleId = otherTrackStudySchedule.id,
                description = "타 트랙 스터디",
                status = StudyStatus.PENDING,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )

        val user = userRepository.save(
            User(
                trackId = userTrack.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-3333-3333",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        // when: 다른 트랙 스터디 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = user.userId,
                studyId = study.id,
            )
        }

        // then: 예외 발생(TRACK_MISMATCH)
        assertEquals(StudyServiceErrorCode.TRACK_MISMATCH.code, thrown.code)
    }

    @ParameterizedTest(name = "신청 상태: {0}")
    @MethodSource("studyStatusCannotBeApplied")
    @DisplayName("교육생이 신청 불가능한 상태의 스터디에 신청한 경우, 예외 발생 - BusinessException(STUDY_NOT_RECRUITING)")
    fun shouldThrowStudyNotRecruiting_whenApplyToStudyCannotBeApplied(caseName: String, givenStudyStatus: StudyStatus, expectedErrorCode: String) {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 모집 기간이 마감되기 전의 일정
         * 3. givenStudyStatus 상태 스터디
         * 4. 교육생
         */
        val today = LocalDate.now()
        val track = trackRepository.save(
            Track(
                trackName = "테스트 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )
        val schedule = studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )

        val study = studyRepository.save(
            Study(
                name = "스터디 이름",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "스터디 설명",
                status = givenStudyStatus,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )

        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-4444-4444",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        // when: 스터디 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = user.userId,
                studyId = study.id,
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(expectedErrorCode, thrown.code)
    }

    @Test
    @DisplayName("교육생이 특정 스터디에 이미 참여중일 때, 해당 교육생이 같은 스터디에 다시 신청하면, 예외 발생 - BusinessException(ALREADY_APPLIED)")
    fun shouldThrowAlreadyApplied_whenApprovedApplicationExists() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(PENDING) 스터디
         * 3. 교육생의 APPROVED 신청 건 존재
         */
        val today = LocalDate.now()
        val track = trackRepository.save(
            Track(
                trackName = "테스트 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )
        studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )

        val leader = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader@gmail.com",
                name = "스터디장",
                phoneNumber = "010-7777-9999",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "승인 중복 신청 스터디",
                description = "승인 중복 신청 스터디",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        val student = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-8888-0000",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        studyRecruitmentService.participate(
            userId = student.userId,
            studyId = studyId,
        )

        // when: 같은 스터디 재신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = student.userId,
                studyId = studyId,
            )
        }

        // then: 예외 발생(ALREADY_APPLIED)
        assertEquals(StudyServiceErrorCode.ALREADY_APPLIED.code, thrown.code)
    }

    // ----- 참여 스터디 수 제한 테스트

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 참여중일 때, 다른 스터디에 추가적인 신청 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
    fun shouldThrowMaxStudyExceeded_whenTwoPendingApplicationsInSameTrack() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 과거 차수 참여 이력
         * 3. 현재 차수 스터디 2개에 PENDING 신청
         */
        val today = LocalDate.now()
        val track = trackRepository.save(
            Track(
                trackName = "테스트 트랙",
                startDate = today.minusDays(30),
                endDate = today.plusDays(30)
            )
        )

        val pastSchedule = studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(60),
                recruitEndDate = today.minusDays(50),
                studyEndDate = today.minusDays(40)
            )
        )
        val currentSchedule = studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.SECOND,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(20)
            )
        )

        val student = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-1000-0000",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val pastStudy = studyRepository.save(
            Study(
                name = "과거 차수 스터디",
                leaderId = 999L,
                trackId = track.trackId,
                scheduleId = pastSchedule.id,
                description = "과거 차수 참여",
                status = StudyStatus.APPROVED,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )
        studyRecruitmentRepository.save(
            StudyRecruitment(
                userId = student.userId,
                study = pastStudy,
            )
        )

        val leader1 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader1@gmail.com",
                name = "리더1",
                phoneNumber = "010-1000-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val leader2 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader2@gmail.com",
                name = "리더2",
                phoneNumber = "010-1000-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val leader3 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader3@gmail.com",
                name = "리더3",
                phoneNumber = "010-1000-0003",
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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val studyId2 = studyService.createStudy(
            StudyCreateCommand(
                userId = leader2.userId,
                name = "현재 차수 스터디2",
                description = "현재 차수 스터디2",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val studyId3 = studyService.createStudy(
            StudyCreateCommand(
                userId = leader3.userId,
                name = "현재 차수 스터디3",
                description = "현재 차수 스터디3",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        studyRecruitmentService.participate(
            userId = student.userId,
            studyId = studyId1,
        )
        studyRecruitmentService.participate(
            userId = student.userId,
            studyId = studyId2,
        )

        entityManager.flush()
        entityManager.clear()

        // when: 다른 스터디 추가 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = student.userId,
                studyId = studyId3,
            )
        }

        // then: 예외 발생(MAX_STUDY_EXCEEDED)
        assertEquals(StudyServiceErrorCode.MAX_STUDY_EXCEEDED.code, thrown.code)
    }

    // ----- 신청 취소 테스트

    // To Do: CLOSED 상태의 스터디에서 취소하려 한 경우, 예외 발생 - BusinessException(?)
    @ParameterizedTest(name = "스터디 상태: {0}")
    @MethodSource("studyStatusCannotBeWithdrawn")
    @DisplayName("스터디장이 아닐 때, PENDING 상태가 아닌 스터디에 참여 중인 신청 건에 대해, 취소를 시도하면, 예외 발생 - BusinessException()")
    fun shouldThrow_when(caseName: String, givenStudyStatus: StudyStatus, expectedErrorCode: String) {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 모집 기간이 마감되기 전의 일정
         * 3. givenStudyStatus 상태 스터디
         * 4. 교육생이 스터디 신청
         */
        val today = LocalDate.now()
        val track = trackRepository.save(
            Track(
                trackName = "테스트 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )
        val schedule = studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )

        val study = studyRepository.save(
            Study(
                name = "스터디 이름",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "스터디 설명",
                status = StudyStatus.PENDING,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )

        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-4444-4444",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.participate(
            userId = user.userId,
            studyId = study.id,
        )

        // 모집 마감
        study.close(LocalDateTime.now().minusDays(1))

        // when: 스터디 취소
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.cancelRecruit(user.userId, recruitmentId)
        }

        // then: 예외 발생
        assertEquals(expectedErrorCode, thrown.code)
    }

    @Test
    @DisplayName("스터디장이 PENDING 상태인 자신의 스터디에서 신청 취소하려는 경우, 예외 발생 - BusinessException(LEADER_CANNOT_LEAVE)")
    fun shouldThrowLeaderCannotLeave_whenLeaderCancelsOwnUnapprovedStudy() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 결재되지 않은(PENDING) 스터디 생성(스터디장은 자동 참여됨)
         * 3. 다른 교육생이 스터디에 신청 및 승인 됨
         */
        val today = LocalDate.now()
        val track = trackRepository.save(
            Track(
                trackName = "테스트 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )
        studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )

        val leader = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader@gmail.com",
                name = "스터디장",
                phoneNumber = "010-9000-0003",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "리더 취소 테스트 스터디",
                description = "리더 취소 테스트 스터디",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        val approvedStudent = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-9000-0004",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        studyRecruitmentService.participate(
            userId = approvedStudent.userId,
            studyId = studyId,
        )

        val leaderRecruitmentId = studyRecruitmentRepository.findAllByStudyId(studyId)
            .first { it.userId == leader.userId }.id

        // when: 스터디장이 자신의 신청 취소 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.cancelRecruit(
                userId = leader.userId,
                recruitmentId = leaderRecruitmentId
            )
        }

        // then: 예외 발생(LEADER_CANNOT_LEAVE)
        assertEquals(StudyServiceErrorCode.LEADER_CANNOT_LEAVE.code, thrown.code)
    }

    // ----- 참여 인원 수 동기화 테스트

    @Test
    @DisplayName("Study.currentMemberCount 와 해당 스터디에 참여중인 StudyRecruitment 레코드 수는 같아야 한다.")
    fun shouldMatchMemberCount() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(PENDING) 스터디
         * 3. 교육생 신청 후 승인
         */
        val today = LocalDate.now()
        val track = trackRepository.save(
            Track(
                trackName = "테스트 트랙",
                startDate = today.minusDays(10),
                endDate = today.plusDays(30)
            )
        )
        studyScheduleRepository.save(
            StudySchedule(
                trackId = track.trackId,
                months = Months.FIRST,
                recruitStartDate = today.minusDays(1),
                recruitEndDate = today.plusDays(1),
                studyEndDate = today.plusDays(10)
            )
        )

        val leader = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader@gmail.com",
                name = "스터디장",
                phoneNumber = "010-9100-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "승인 카운트 스터디",
                description = "승인 카운트 스터디",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        // 세 명의 교육생이 참여
        val student1 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student1@gmail.com",
                name = "교육생1",
                phoneNumber = "010-9100-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        studyRecruitmentService.participate(
            userId = student1.userId,
            studyId = studyId,
        )

        val student2 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student2@gmail.com",
                name = "교육생2",
                phoneNumber = "010-9100-0003",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        studyRecruitmentService.participate(
            userId = student2.userId,
            studyId = studyId,
        )

        val student3 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student3@gmail.com",
                name = "교육생3",
                phoneNumber = "010-9100-0004",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        studyRecruitmentService.participate(
            userId = student3.userId,
            studyId = studyId,
        )

        entityManager.flush()
        entityManager.clear()

        // then: 승인된 신청 건 수 == 현재 참여 인원 수 == 4 (리더 포함)
        val updatedStudy = studyRepository.findById(studyId).orElseThrow()
        val approvedCount = studyRecruitmentRepository.findAllByStudyId(studyId).size
        assertEquals(4, approvedCount)
        assertEquals(approvedCount, updatedStudy.currentMemberCount)
    }
}
