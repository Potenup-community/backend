package kr.co.wground.study.application

import jakarta.persistence.EntityManager
import java.time.LocalDate
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyCreateCommand
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
import kr.co.wground.study_schedule.application.exception.StudyScheduleServiceErrorCode
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
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
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
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
            Arguments.of("RECRUITING_CLOSED", StudyStatus.RECRUITING_CLOSED, StudyServiceErrorCode.STUDY_NOT_RECRUITING.code),
            Arguments.of("IN_PROGRESS", StudyStatus.IN_PROGRESS, StudyServiceErrorCode.STUDY_NOT_RECRUITING.code),
        )

        @JvmStatic
        fun studyStatusCannotBeWithdrawn(): Stream<Arguments> = Stream.of(
            Arguments.of("RECRUITING_CLOSED", StudyStatus.RECRUITING_CLOSED, StudyDomainErrorCode.RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_RECRUITING.code),
            Arguments.of("IN_PROGRESS", StudyStatus.IN_PROGRESS, StudyDomainErrorCode.RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_RECRUITING.code),
        )
    }

    // ----- 참여 테스트

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

        val study = Study.createNew(
            name = "졸업 트랙 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedSchedule.id,
            description = "졸업 트랙 스터디 설명",
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
            Study.createNew(
                name = "타 트랙 스터디",
                leaderId = 10L,
                trackId = otherTrack.trackId,
                scheduleId = otherTrackStudySchedule.id,
                description = "타 트랙 스터디",
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

    @Test
    @DisplayName("교육생이 RECRUITING_CLOSED 상태의 스터디에 신청한 경우, 예외 발생 - BusinessException(STUDY_ALREADY_FINISH_TO_RECRUIT)")
    fun shouldThrowStudyNotRecruiting_whenApplyToClosedStudy() {

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
            Study.createNew(
                name = "스터디 이름",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "스터디 설명",
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
        )

        val participant = userRepository.save(
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
        study.participate(participant.userId)

        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = null,
            newRecruitEnd = LocalDate.now().minusDays(1),
            newStudyEnd = null
        )
        studyScheduleRepository.save(schedule)

        study.closeRecruitment()
        studyRepository.save(study)

        // when: 스터디 신청
        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "late@gmail.com",
                name = "교육생",
                phoneNumber = "010-3333-3333",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = user.userId,
                studyId = study.id,
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(StudyScheduleServiceErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 IN_PROGRESS 상태의 스터디에 신청한 경우, 예외 발생 - BusinessException(STUDY_ALREADY_FINISH_TO_RECRUIT)")
    fun shouldThrowStudyNotRecruiting_whenApplyToApprovedStudy() {

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
            Study.createNew(
                name = "스터디 이름",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "스터디 설명",
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
        )

        val participant = userRepository.save(
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
        study.participate(participant.userId)

        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = null,
            newRecruitEnd = LocalDate.now().minusDays(1),
            newStudyEnd = null
        )
        studyScheduleRepository.save(schedule)

        study.closeRecruitment()
        study.start()
        studyRepository.save(study)

        // when: 스터디 신청
        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "late@gmail.com",
                name = "교육생",
                phoneNumber = "010-3333-3333",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.participate(
                userId = user.userId,
                studyId = study.id,
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(StudyScheduleServiceErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 특정 스터디에 이미 참여중일 때, 해당 교육생이 같은 스터디에 다시 신청하면, 예외 발생 - BusinessException(ALREADY_APPLIED)")
    fun shouldThrowAlreadyApplied_whenApplicantAlreadyExists() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(RECRUITING) 스터디
         * 3. 교육생의 IN_PROGRESS 신청 건 존재
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
                name = "참여 중복 신청 스터디",
                description = "참여 중복 신청 스터디",
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
        assertEquals(StudyDomainErrorCode.ALREADY_APPLIED.code, thrown.code)
    }

    @Test
    @DisplayName("이미 모집 기간이 마감된 경우(스터디 상태가 RECRUITING_CLOSED 일 때), 참여 시, 예외 발생 - BusinessException(STUDY_ALREADY_FINISH_TO_RECRUIT)")
    fun shouldThrowStudyAlreadyFinishToRecruit_whenIncreaseMemberAfterRecruitEnd() {

        val thrown = assertThrows<BusinessException> {

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

            val leader = User(
                trackId = track.trackId,
                email = "test@gmail.com",
                name = "스터디장",
                phoneNumber = "010-5555-5555",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
            val savedLeader = userRepository.save(leader)

            val student1 = User(
                trackId = track.trackId,
                email = "student1@gmail.com",
                name = "참가자",
                phoneNumber = "010-4444-4444",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
            val savedStudent1 = userRepository.save(student1)

            val study = Study.createNew(
                name = "삭제 테스트 스터디(RECRUITING)",
                leaderId = leader.userId,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "삭제 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                budgetExplain = "🍕🍕🍕",
                weeklyPlans = WeeklyPlans.of(
                    week1Plan = "1주차 계획",
                    week2Plan = "2주차 계획",
                    week3Plan = "3주차 계획",
                    week4Plan = "4주차 계획",
                ),
                externalChatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                referenceUrl = null,
            )
            study.participate(student1.userId)
            study.closeRecruitment()
            studyRepository.save(study)

            entityManager.flush()
            entityManager.clear()

            val found = studyRepository.findByIdOrNull(study.id) ?: fail("알 수 없는 이유로 스터디가 생성되지 않았습니다.")

            // when
            val student2 = User(
                trackId = track.trackId,
                email = "student2@gmail.com",
                name = "참가자",
                phoneNumber = "010-4444-4444",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
            val savedStudent2 = userRepository.save(student2)

            schedule.updateSchedule(
                newMonths = null,
                newRecruitStart = null,
                newRecruitEnd = LocalDate.now().minusDays(1),
                newStudyEnd = null
            )
            studyScheduleRepository.save(schedule)
            studyRecruitmentService.participate(userId = student2.userId, studyId = found.id)
        }

        assertEquals(StudyScheduleServiceErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT.code, thrown.code)
    }

    // 강제 참여 테스트

    @Test
    @DisplayName("이미 모집 기간이 마감된 경우(스터디 상태가 RECRUITING_CLOSED 일 때), 강제 참여 시키는 것이 가능하다.")
    fun shouldSuccess_whenForceJoinToStudyWhichIsClosed() {

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

        val leader = User(
            trackId = track.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-5555-5555",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        val student1 = User(
            trackId = track.trackId,
            email = "student1@gmail.com",
            name = "참가자",
            phoneNumber = "010-4444-4444",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedStudent1 = userRepository.save(student1)

        val study = Study.createNew(
            name = "삭제 테스트 스터디(RECRUITING)",
            leaderId = leader.userId,
            trackId = track.trackId,
            scheduleId = schedule.id,
            description = "삭제 테스트",
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "🍕🍕🍕",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            externalChatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
            referenceUrl = null,
        )
        study.participate(student1.userId)
        study.closeRecruitment()
        studyRepository.save(study)

        entityManager.flush()
        entityManager.clear()

        val found = studyRepository.findByIdOrNull(study.id) ?: fail("알 수 없는 이유로 스터디가 생성되지 않았습니다.")

        // when
        val student2 = User(
            trackId = track.trackId,
            email = "student2@gmail.com",
            name = "참가자",
            phoneNumber = "010-4444-4444",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedStudent2 = userRepository.save(student2)

        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = null,
            newRecruitEnd = LocalDate.now().minusDays(1),
            newStudyEnd = null
        )
        studyScheduleRepository.save(schedule)
        val forceJoinedRecruitmentId = studyRecruitmentService.forceJoin(userId = student2.userId, studyId = found.id)

        // then
        assertNotNull(studyRecruitmentRepository.findByIdOrNull(forceJoinedRecruitmentId))
    }

    @Test
    @DisplayName("수료생을 스터디에 강제 참여 시킨 경우, 예외 발생 - BusinessException(GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY)")
    fun shouldThrowGraduatedStudentCantRecruitOfficialStudy_whenForceJoinGraduatedStudent() {
        
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

        val study = Study.createNew(
            name = "졸업 트랙 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedSchedule.id,
            description = "졸업 트랙 스터디 설명",
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

        // when: 수료생 강제 참여 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.forceJoin(
                userId = savedUser.userId,
                studyId = savedStudy.id,
            )
        }

        // then: 예외 발생(TRACK_IS_NOT_ENROLLED)
        assertEquals(StudyServiceErrorCode.GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY.code, thrown.code)
    }

    @Test
    @DisplayName("교육생을 자신의 트랙이 아닌 스터디에 강제 참여 시킨 경우, 예외 발생 - BusinessException(TRACK_MISMATCH)")
    fun shouldThrowTrackMismatch_whenForceJoinApplicantWhichHasDifferentTrack() {

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
            Study.createNew(
                name = "타 트랙 스터디",
                leaderId = 10L,
                trackId = otherTrack.trackId,
                scheduleId = otherTrackStudySchedule.id,
                description = "타 트랙 스터디",
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
            studyRecruitmentService.forceJoin(
                userId = user.userId,
                studyId = study.id,
            )
        }

        // then: 예외 발생(TRACK_MISMATCH)
        assertEquals(StudyServiceErrorCode.TRACK_MISMATCH.code, thrown.code)
    }

    @Test
    @DisplayName("교육생을 IN_PROGRESS 상태의 스터디에 강제 참여 시킨 경우, 예외 발생 - BusinessException(STUDY_ALREADY_FINISH_TO_RECRUIT)")
    fun shouldThrowSStudyCannotForceJoinAfterApproval_whenForceJoinAfterApproved() {

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
            Study.createNew(
                name = "스터디 이름",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "스터디 설명",
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
        )

        val participant = userRepository.save(
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
        study.participate(participant.userId)

        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = null,
            newRecruitEnd = LocalDate.now().minusDays(1),
            newStudyEnd = null
        )
        studyScheduleRepository.save(schedule)

        study.closeRecruitment()
        study.start()
        studyRepository.save(study)

        // when: 스터디 신청
        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "late@gmail.com",
                name = "교육생",
                phoneNumber = "010-3333-3333",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.forceJoin(
                userId = user.userId,
                studyId = study.id,
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(StudyDomainErrorCode.CANNOT_FORCE_JOIN_IN_PROGRESS_OR_COMPLETED.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 특정 스터디에 이미 참여중일 때, 해당 교육생을 같은 스터디에 다시 강제 참여 시키면, 예외 발생 - BusinessException(ALREADY_APPLIED)")
    fun shouldThrowAlreadyApplied_whenForceJoinApplicant() {

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
                name = "참여 중복 신청 스터디",
                description = "참여 중복 신청 스터디",
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
            studyRecruitmentService.forceJoin(
                userId = student.userId,
                studyId = studyId,
            )
        }

        // then: 예외 발생(ALREADY_APPLIED)
        assertEquals(StudyDomainErrorCode.ALREADY_APPLIED.code, thrown.code)
    }

    // ----- 참여 스터디 수 제한 테스트

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 참여중일 때, 다른 스터디에 추가적인 신청 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
    fun shouldThrowMaxStudyExceeded_whenTwoPendingApplicationsInSameTrack() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 과거 차수 참여 이력
         * 3. 현재 차수 스터디 2개에 RECRUITING 신청
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
            Study.createNew(
                name = "과거 차수 스터디",
                leaderId = 999L,
                trackId = track.trackId,
                scheduleId = pastSchedule.id,
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
        )
        studyRecruitmentRepository.save(
            StudyRecruitment.apply(
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
                tags = emptyList(),
            )
        )
        val studyId3 = studyService.createStudy(
            StudyCreateCommand(
                userId = leader3.userId,
                name = "현재 차수 스터디3",
                description = "현재 차수 스터디3",
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

    // To Do: RECRUITING_CLOSED 상태의 스터디에서 취소하려 한 경우, 예외 발생 - BusinessException(?)
    @ParameterizedTest(name = "스터디 상태: {0}")
    @MethodSource("studyStatusCannotBeWithdrawn")
    @DisplayName("스터디장이 아닐 때, RECRUITING 상태가 아닌 스터디에 참여 중인 신청 건에 대해, 취소를 시도하면, 예외 발생 - BusinessException()")
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
            Study.createNew(
                name = "스터디 이름",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "스터디 설명",
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
        study.closeRecruitment()

        // when: 스터디 취소
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.cancelRecruit(user.userId, recruitmentId)
        }

        // then: 예외 발생
        assertEquals(expectedErrorCode, thrown.code)
    }

    @Test
    @DisplayName("스터디장이 RECRUITING 상태인 자신의 스터디에서 신청 취소하려는 경우, 예외 발생 - BusinessException(LEADER_CANNOT_LEAVE)")
    fun shouldThrowLeaderCannotLeave_whenLeaderCancelsOwnUnapprovedStudy() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 진행 시작되지 않은(RECRUITING) 스터디 생성(스터디장은 자동 참여됨)
         * 3. 다른 교육생이 스터디에 신청 및 참여함
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
        assertEquals(StudyDomainErrorCode.LEADER_CANNOT_LEAVE.code, thrown.code)
    }

    // ----- 참여 인원 수 동기화 테스트

    @Test
    @DisplayName("Study.currentMemberCount 와 해당 스터디에 참여중인 StudyRecruitment 레코드 수는 같아야 한다.")
    fun shouldMatchMemberCount() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(RECRUITING) 스터디
         * 3. 교육생 신청 후 참여
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
                name = "참여 카운트 스터디",
                description = "참여 카운트 스터디",
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

        // then: 참여 신청 건 수 == 현재 참여 인원 수 == 4 (리더 포함)
        val updatedStudy = studyRepository.findById(studyId).orElseThrow()
        val approvedCount = studyRecruitmentRepository.findAllByStudyId(studyId).size
        assertEquals(4, approvedCount)
        assertEquals(approvedCount, updatedStudy.recruitments.size)
    }
}
