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
import kr.co.wground.study.domain.constant.RecruitStatus
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
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

    // ----- 신청 테스트

    // To Do: 조기 수료 가능한 지 아름님한테 물어볼 것
    @Test
    @DisplayName("수료생이 스터디에 신청한 경우, 예외 발생 - BusinessException(GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY)")
    fun graduate_cannot_apply_study() {

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
            studyRecruitmentService.requestRecruit(
                userId = savedUser.userId,
                studyId = savedStudy.id,
                appeal = "참여하고 싶습니다."
            )
        }

        // then: 예외 발생(TRACK_IS_NOT_ENROLLED)
        assertEquals(StudyServiceErrorCode.GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 모집 기간이 마감되기 전의 모집 중(PENDING)인 스터디에, 신청 후 승인되었을 때, 이로 인해 해당 스터디가 정원에 도달하면, 그 스터디는 CLOSED 상태로 변경되어야 한다")
    fun approved_application_closes_study_on_capacity() {
        
        /*
         * given
         * 1. ENROLLED 상태의 트랙
         * 2. 모집 중인 스터디 일정(모집 마감 전)
         * 3. 모집 중(PENDING)이며 정원 2명인 스터디(리더 1명 포함)
         * 4. 교육생 1명
         */
        val today = LocalDate.now()
        val track = Track(
            trackName = "테스트 트랙",
            startDate = today.minusDays(10),
            endDate = today.plusDays(30)
        )
        val savedTrack = trackRepository.save(track)

        val schedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.FIRST,
            recruitStartDate = today.minusDays(1),
            recruitEndDate = today.plusDays(1),
            studyEndDate = today.plusDays(20)
        )
        val savedSchedule = studyScheduleRepository.save(schedule)

        val leader = userRepository.save(
            User(
                trackId = savedTrack.trackId,
                email = "leader@gmail.com",
                name = "스터디장",
                phoneNumber = "010-1111-1111",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "모집 중 스터디",
                description = "모집 중 스터디",
                capacity = 2,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findById(studyId).orElseThrow()

        val student = userRepository.save(
            User(
                trackId = savedTrack.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-2222-2222",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        // when: 교육생이 스터디 신청 후 승인
        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = savedStudy.id,
            appeal = "참여 희망"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // then: 정원 도달로 스터디가 CLOSED 상태로 변경
        entityManager.flush()
        entityManager.clear()
        val updatedStudy = studyRepository.findById(savedStudy.id).orElseThrow()
        assertEquals(StudyStatus.CLOSED, updatedStudy.status)
    }

    @Test
    @DisplayName("교육생이 자신의 트랙이 아닌 스터디에 신청한 경우, 예외 발생 - BusinessException(TRACK_MISMATCH)")
    fun track_mismatch_application_throws() {

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
            studyRecruitmentService.requestRecruit(
                userId = user.userId,
                studyId = study.id,
                appeal = "신청합니다."
            )
        }

        // then: 예외 발생(TRACK_MISMATCH)
        assertEquals(StudyServiceErrorCode.TRACK_MISMATCH.code, thrown.code)
    }

    // To Do: 나중에 모집 기간이 마감된 경우와, 모집 기간이 마감되지 않았지만 정원이 가득찬 경우를 나누어서 예외 처리 하는 것도 고려해보자.
    @Test
    @DisplayName("교육생이 CLOSED 상태의 스터디에 신청한 경우, 예외 발생 - BusinessException(STUDY_NOT_RECRUITING)")
    fun apply_to_closed_study_throws() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 모집 기간이 마감되기 전의 일정
         * 3. CLOSED 상태 스터디
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
                name = "CLOSED 스터디",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "CLOSED 상태",
                status = StudyStatus.CLOSED,
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

        // when: CLOSED 스터디 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = user.userId,
                studyId = study.id,
                appeal = "신청합니다."
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(StudyServiceErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 REJECTED 상태의 스터디에 신청한 경우, 예외 발생 - BusinessException(STUDY_NOT_RECRUITING)")
    fun apply_to_rejected_study_throws() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 모집 기간이 마감되기 전의 일정
         * 3. REJECTED 상태 스터디
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
                name = "REJECTED 스터디",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "REJECTED 상태",
                status = StudyStatus.REJECTED,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )

        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-5555-5555",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        // when: REJECTED 스터디 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = user.userId,
                studyId = study.id,
                appeal = "신청합니다."
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(StudyServiceErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
    }

    @Test
    @DisplayName("교육생이 APPROVED 상태의 스터디에 신청한 경우, 예외 발생 - BusinessException(STUDY_NOT_RECRUITING)")
    fun apply_to_approved_study_throws() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 모집 기간이 마감되기 전의 일정
         * 3. APPROVED 상태 스터디
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
                name = "APPROVED 스터디",
                leaderId = 10L,
                trackId = track.trackId,
                scheduleId = schedule.id,
                description = "APPROVED 상태",
                status = StudyStatus.APPROVED,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )

        val user = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student@gmail.com",
                name = "교육생",
                phoneNumber = "010-6666-6666",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        // when: APPROVED 스터디 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = user.userId,
                studyId = study.id,
                appeal = "신청합니다."
            )
        }

        // then: 예외 발생(STUDY_NOT_RECRUITING)
        assertEquals(StudyServiceErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
    }

    @Test
    @DisplayName("특정 교육생의 PENDING 상태의 신청 건이 존재할 때, 해당 교육생이 같은 스터디에 다시 신청하면, 예외 발생 - BusinessException(ALREADY_APPLIED)")
    fun duplicate_pending_application_throws() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(PENDING) 스터디
         * 3. 교육생의 PENDING 신청 건 존재
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
                phoneNumber = "010-7777-7777",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "중복 신청 스터디",
                description = "중복 신청 스터디",
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
                phoneNumber = "010-7777-8888",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "첫 신청"
        )

        // when: 같은 스터디 재신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = student.userId,
                studyId = studyId,
                appeal = "재신청"
            )
        }

        // then: 예외 발생(ALREADY_APPLIED)
        assertEquals(StudyServiceErrorCode.ALREADY_APPLIED.code, thrown.code)
    }

    @Test
    @DisplayName("특정 교육생의 APPROVED 상태의 신청 건이 존재할 때, 해당 교육생이 같은 스터디에 다시 신청하면, 예외 발생 - BusinessException(ALREADY_APPLIED)")
    fun duplicate_approved_application_throws() {

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

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "첫 신청"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // when: 같은 스터디 재신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = student.userId,
                studyId = studyId,
                appeal = "재신청"
            )
        }

        // then: 예외 발생(ALREADY_APPLIED)
        assertEquals(StudyServiceErrorCode.ALREADY_APPLIED.code, thrown.code)
    }

    @Test
    @DisplayName("특정 교육생의 CANCELLED 상태의 신청 건이 존재해도, 해당 교육생은 같은 스터디에 다시 신청 가능하다")
    fun reapply_after_cancelled_application_allowed() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(PENDING) 스터디
         * 3. 교육생의 CANCELLED 신청 건 존재
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
                phoneNumber = "010-8888-1111",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "취소 후 재신청 스터디",
                description = "취소 후 재신청 스터디",
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
                phoneNumber = "010-8888-2222",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "첫 신청"
        )
        studyRecruitmentService.cancelRecruit(student.userId, recruitmentId)

        // when: 같은 스터디 재신청
        val newRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "재신청"
        )

        // then: 재신청 성공(PENDING)
        val savedRecruitment = studyRecruitmentRepository.findById(newRecruitmentId).orElseThrow()
        assertEquals(RecruitStatus.PENDING, savedRecruitment.recruitStatus)
    }

    @Test
    @DisplayName("특정 교육생의 REJECTED 상태의 신청 건이 존재해도, 해당 교육생은 같은 스터디에 다시 신청 가능하다")
    fun reapply_after_rejected_application_allowed() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(PENDING) 스터디
         * 3. 교육생의 REJECTED 신청 건 존재
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
                phoneNumber = "010-8888-3333",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "반려 후 재신청 스터디",
                description = "반려 후 재신청 스터디",
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
                phoneNumber = "010-8888-4444",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "첫 신청"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId,
            newStatus = RecruitStatus.REJECTED
        )

        // when: 같은 스터디 재신청
        val newRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "재신청"
        )

        // then: 재신청 성공(PENDING)
        val savedRecruitment = studyRecruitmentRepository.findById(newRecruitmentId).orElseThrow()
        assertEquals(RecruitStatus.PENDING, savedRecruitment.recruitStatus)
    }

    // ----- 참여 스터디 수 제한 테스트

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 모두 PENDING 상태일 때, 다른 스터디에 추가적인 신청 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
    fun pending_two_applications_same_track_blocks_third() {

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
                appeal = "과거 참여",
                recruitStatus = RecruitStatus.APPROVED,
                approvedAt = LocalDateTime.now().minusDays(30)
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

        studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId1,
            appeal = "신청1"
        )
        studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId2,
            appeal = "신청2"
        )

        entityManager.flush()
        entityManager.clear()

        // when: 다른 스터디 추가 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = student.userId,
                studyId = studyId3,
                appeal = "추가 신청"
            )
        }

        // then: 예외 발생(MAX_STUDY_EXCEEDED)
        assertEquals(StudyServiceErrorCode.MAX_STUDY_EXCEEDED.code, thrown.code)
    }

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 모두 APPROVED 상태일 때, 다른 스터디에 추가적인 신청 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
    fun approved_two_applications_same_track_blocks_third() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 과거 차수 참여 이력
         * 3. 현재 차수 스터디 2개에 APPROVED 신청
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
                phoneNumber = "010-2000-0000",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val pastStudy = studyRepository.save(
            Study(
                name = "과거 차수 스터디",
                leaderId = 998L,
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
                appeal = "과거 참여",
                recruitStatus = RecruitStatus.APPROVED,
                approvedAt = LocalDateTime.now().minusDays(30)
            )
        )

        val leader1 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader1@gmail.com",
                name = "리더1",
                phoneNumber = "010-2000-0001",
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
                phoneNumber = "010-2000-0002",
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
                phoneNumber = "010-2000-0003",
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

        val recruitmentId1 = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId1,
            appeal = "신청1"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader1.userId,
            recruitmentId = recruitmentId1,
            newStatus = RecruitStatus.APPROVED
        )

        val recruitmentId2 = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId2,
            appeal = "신청2"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader2.userId,
            recruitmentId = recruitmentId2,
            newStatus = RecruitStatus.APPROVED
        )

        entityManager.flush()
        entityManager.clear()

        // when: 다른 스터디 추가 신청
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.requestRecruit(
                userId = student.userId,
                studyId = studyId3,
                appeal = "추가 신청"
            )
        }

        // then: 예외 발생(MAX_STUDY_EXCEEDED)
        assertEquals(StudyServiceErrorCode.MAX_STUDY_EXCEEDED.code, thrown.code)
    }

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 중 하나는 PENDING 상태, 다른 하나는 REJECTED 상태 인 경우, 다른 현재 차수 스터디에 추가 신청 가능하다")
    fun pending_and_rejected_allows_additional_application() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 과거 차수 참여 이력
         * 3. 현재 차수 스터디 2개에 PENDING/REJECTED 신청
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
                phoneNumber = "010-3000-0000",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val pastStudy = studyRepository.save(
            Study(
                name = "과거 차수 스터디",
                leaderId = 997L,
                trackId = track.trackId,
                scheduleId = pastSchedule.id,
                description = "과거 차수 참여",
                status = StudyStatus.PENDING,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )
        studyRecruitmentRepository.save(
            StudyRecruitment(
                userId = student.userId,
                study = pastStudy,
                appeal = "과거 참여",
                recruitStatus = RecruitStatus.APPROVED,
                approvedAt = LocalDateTime.now().minusDays(30)
            )
        )

        val leader1 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader1@gmail.com",
                name = "리더1",
                phoneNumber = "010-3000-0001",
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
                phoneNumber = "010-3000-0002",
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
                phoneNumber = "010-3000-0003",
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

        studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId1,
            appeal = "신청1"
        )

        val recruitmentId2 = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId2,
            appeal = "신청2"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader2.userId,
            recruitmentId = recruitmentId2,
            newStatus = RecruitStatus.REJECTED
        )

        entityManager.flush()
        entityManager.clear()

        // when: 다른 스터디 추가 신청
        val newRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId3,
            appeal = "추가 신청"
        )

        // then: 추가 신청 성공(PENDING)
        val savedRecruitment = studyRecruitmentRepository.findById(newRecruitmentId).orElseThrow()
        assertEquals(RecruitStatus.PENDING, savedRecruitment.recruitStatus)
    }

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 중 하나는 PENDING 상태, 다른 하나는 CANCELLED 상태 인 경우, 다른 현재 차수 스터디에 추가 신청 가능하다")
    fun pending_and_cancelled_allows_additional_application() {

        /*
         * given
         * 1. ENROLLED 트랙
         * 2. 과거 차수 참여 이력
         * 3. 현재 차수 스터디 2개에 PENDING/CANCELLED 신청
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
                phoneNumber = "010-4000-0000",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val pastStudy = studyRepository.save(
            Study(
                name = "과거 차수 스터디",
                leaderId = 996L,
                trackId = track.trackId,
                scheduleId = pastSchedule.id,
                description = "과거 차수 참여",
                status = StudyStatus.PENDING,
                capacity = 5,
                budget = BudgetType.MEAL
            )
        )
        studyRecruitmentRepository.save(
            StudyRecruitment(
                userId = student.userId,
                study = pastStudy,
                appeal = "과거 참여",
                recruitStatus = RecruitStatus.APPROVED,
                approvedAt = LocalDateTime.now().minusDays(30)
            )
        )

        val leader1 = userRepository.save(
            User(
                trackId = track.trackId,
                email = "leader1@gmail.com",
                name = "리더1",
                phoneNumber = "010-4000-0001",
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
                phoneNumber = "010-4000-0002",
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
                phoneNumber = "010-4000-0003",
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

        studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId1,
            appeal = "신청1"
        )

        val toBeCancelledRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId2,
            appeal = "취소 예정 신청"
        )
        studyRecruitmentService.cancelRecruit(userId = student.userId, toBeCancelledRecruitmentId)

        entityManager.flush()
        entityManager.clear()

        // when: 다른 스터디 추가 신청
        val newRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId3,
            appeal = "추가 신청"
        )

        // then: 추가 신청 성공(PENDING)
        val savedRecruitment = studyRecruitmentRepository.findById(newRecruitmentId).orElseThrow()
        assertEquals(RecruitStatus.PENDING, savedRecruitment.recruitStatus)
    }

    // ----- 신청 취소 테스트

    @Test
    @DisplayName("스터디장이 아닐 때, 결재되지 않은 스터디에서 신청 취소가능하다")
    fun non_leader_can_cancel_before_approval() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 결재되지 않은(PENDING) 스터디
         * 3. 스터디장이 아닌 교육생의 신청 건
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
                phoneNumber = "010-9999-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "취소 테스트 스터디",
                description = "취소 테스트 스터디",
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
                phoneNumber = "010-9999-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )

        val approvedStudent = userRepository.save(
            User(
                trackId = track.trackId,
                email = "student2@gmail.com",
                name = "교육생2",
                phoneNumber = "010-9999-0003",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val approvedRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = approvedStudent.userId,
            studyId = studyId,
            appeal = "승인 신청"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = approvedRecruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // when: 스터디장이 아닌 신청자가 취소
        studyRecruitmentService.cancelRecruit(
            userId = student.userId,
            recruitmentId = recruitmentId
        )
        studyRecruitmentService.cancelRecruit(
            userId = approvedStudent.userId,
            recruitmentId = approvedRecruitmentId
        )

        // then: 신청 상태가 CANCELLED로 변경
        entityManager.flush()
        entityManager.clear()
        val savedRecruitment = studyRecruitmentRepository.findById(recruitmentId).orElseThrow()
        val savedApprovedRecruitment = studyRecruitmentRepository.findById(approvedRecruitmentId).orElseThrow()
        assertEquals(RecruitStatus.CANCELLED, savedRecruitment.recruitStatus)
        assertEquals(RecruitStatus.CANCELLED, savedApprovedRecruitment.recruitStatus)
    }

    @Test
    @DisplayName("결재된 스터디에서 신청 취소하려는 경우, 예외 발생 - BusinessException(RECRUITMENT_STATUS_CANT_CHANGE_IN_DETERMINE)")
    fun cancel_after_study_approved_throws() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중인 스터디(정원 2명) 생성
         * 3. 해당 스터디에 신청 후 신청 승인 됨
         * 4. 해당 스터디가 관리자에 의해 결재 됨
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
                phoneNumber = "010-9000-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "결재 스터디",
                description = "결재 스터디",
                capacity = 2,
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
                phoneNumber = "010-9000-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // 관리자 결재 처리
        studyService.approveStudy(studyId)

        // when: 결재된 스터디 신청 취소 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.cancelRecruit(
                userId = student.userId,
                recruitmentId = recruitmentId
            )
        }

        // then: 예외 발생(RECRUITMENT_STATUS_CANT_CHANGE_IN_DETERMINE)
        assertEquals(StudyDomainErrorCode.RECRUITMENT_STATUS_CANT_CHANGE_IN_DETERMINE.code, thrown.code)
    }

    @Test
    @DisplayName("스터디장이 아직 결재되지 않은 자신의 스터디에서 신청 취소하려는 경우, 예외 발생 - BusinessException(LEADER_CANNOT_LEAVE)")
    fun leader_cannot_cancel_own_unapproved_study() {

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
        val approvedRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = approvedStudent.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = approvedRecruitmentId,
            newStatus = RecruitStatus.APPROVED
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

    @Test
    @DisplayName("REJECTED 상태인 신청 건에 대해, 취소하려 한 경우, 예외 발생 - BusinessException(RECRUITMENT_INVALID_STATUS_CHANGE)")
    fun cancel_rejected_application_throws() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 결재되지 않은(PENDING) 스터디
         * 3. 교육생 신청 후 REJECTED 처리된 신청 건
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
                phoneNumber = "010-9000-0005",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "반려된 신청 취소 테스트 스터디",
                description = "반려된 신청 취소 테스트",
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
                phoneNumber = "010-9000-0006",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId,
            newStatus = RecruitStatus.REJECTED
        )

        // when: REJECTED 신청 건 취소 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.cancelRecruit(
                userId = student.userId,
                recruitmentId = recruitmentId
            )
        }

        // then: 예외 발생(RECRUITMENT_INVALID_STATUS_CHANGE)
        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    @DisplayName("모집 기간이 마감되지 않았고, 정원에 도달한, 상태가 CLOSED 인 스터디에 대해, 승인된 인원이 신청을 취소하면 스터디 상태는 PENDING 상태로 변경되어야 한다")
    fun approved_cancellation_reopens_closed_study() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정(모집 마감 전)
         * 2. 정원 2명 스터디 생성(스터디장 자동 참여)
         * 3. 교육생 신청 후 승인 -> 정원 도달로 CLOSED
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
                phoneNumber = "010-9000-0007",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "정원 도달 스터디",
                description = "정원 도달 스터디",
                capacity = 2,
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
                phoneNumber = "010-9000-0008",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val approvedRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = approvedStudent.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = approvedRecruitmentId,
            newStatus = RecruitStatus.APPROVED
        )
        
        // 현재 정원(2) 도달되어 closed 된 상태임

        // when: 승인된 인원이 신청 취소
        studyRecruitmentService.cancelRecruit(
            userId = approvedStudent.userId,
            recruitmentId = approvedRecruitmentId
        )

        // then: 스터디 상태가 PENDING으로 변경
        entityManager.flush()
        entityManager.clear()
        val updatedStudy = studyRepository.findById(studyId).orElseThrow()
        val updatedRecruitment = studyRecruitmentRepository.findById(approvedRecruitmentId).orElseThrow()
        assertEquals(StudyStatus.PENDING, updatedStudy.status)
        assertEquals(RecruitStatus.CANCELLED, updatedRecruitment.recruitStatus)
    }

    @Test
    @DisplayName("모집 기간이 마감되지 않았고, 정원에 도달한, 상태가 CLOSED 인 스터디에 대해, PENDING 상태의 신청이 취소되어도 스터디 상태는 CLOSED 상태로 유지되어야 한다")
    fun pending_cancellation_keeps_closed_study() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정(모집 마감 전)
         * 2. 정원 2명 스터디 생성(스터디장 자동 참여)
         * 3. 교육생 1명 PENDING 신청
         * 4. 다른 교육생 신청 후 승인 -> 정원 도달로 CLOSED
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
                phoneNumber = "010-9000-0009",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "정원 도달 스터디",
                description = "정원 도달 스터디",
                capacity = 2,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        val pendingStudent = userRepository.save(
            User(
                trackId = track.trackId,
                email = "pending@gmail.com",
                name = "대기 교육생",
                phoneNumber = "010-9000-0010",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val pendingRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = pendingStudent.userId,
            studyId = studyId,
            appeal = "대기 신청"
        )

        val approvedStudent = userRepository.save(
            User(
                trackId = track.trackId,
                email = "approved@gmail.com",
                name = "승인 교육생",
                phoneNumber = "010-9000-0011",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val approvedRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = approvedStudent.userId,
            studyId = studyId,
            appeal = "승인 신청"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = approvedRecruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // when: PENDING 신청 취소
        studyRecruitmentService.cancelRecruit(
            userId = pendingStudent.userId,
            recruitmentId = pendingRecruitmentId
        )

        // then: 스터디 상태는 CLOSED 유지
        entityManager.flush()
        entityManager.clear()
        val updatedStudy = studyRepository.findById(studyId).orElseThrow()
        val updatedRecruitment = studyRecruitmentRepository.findById(pendingRecruitmentId).orElseThrow()
        assertEquals(StudyStatus.CLOSED, updatedStudy.status)
        assertEquals(RecruitStatus.CANCELLED, updatedRecruitment.recruitStatus)
    }

    // ----- 신청 승인 테스트

    @Test
    @DisplayName("승인된 신청 건과, 스터디의 현재 참여 인원 수는 같아야 한다")
    fun approved_count_matches_member_count() {

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

        // 세 명의 교육생이 신청
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

        // 세 건의 신청은 각각 PENDING, APPROVED, REJECTED 상태임
        val recruitmentId1 = studyRecruitmentService.requestRecruit(
            userId = student1.userId,
            studyId = studyId,
            appeal = "신청1"
        )
        val recruitmentId2 = studyRecruitmentService.requestRecruit(
            userId = student2.userId,
            studyId = studyId,
            appeal = "신청2"
        )
        val recruitmentId3 = studyRecruitmentService.requestRecruit(
            userId = student3.userId,
            studyId = studyId,
            appeal = "신청2"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId1,
            newStatus = RecruitStatus.APPROVED
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId2,
            newStatus = RecruitStatus.REJECTED
        )

        // then: 승인된 신청 건 수 == 현재 참여 인원 수 == 2
        entityManager.flush()
        entityManager.clear()
        val updatedStudy = studyRepository.findById(studyId).orElseThrow()
        val approvedCount = studyRecruitmentRepository.findAllByStudyId(studyId)
            .count { it.recruitStatus == RecruitStatus.APPROVED }
        assertEquals(2, approvedCount)
        assertEquals(approvedCount, updatedStudy.currentMemberCount)
    }

    @Test
    @DisplayName("정원이 가득 찬 스터디에 대해, 기존의 신청 건을 승인하면, 예외 발생 - BusinessException(STUDY_CAPACITY_FULL)")
    fun approve_when_full_throws() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 정원 2명 스터디(리더 포함)
         * 3. 교육생 1명 승인되어 정원 도달
         * 4. 다른 교육생의 PENDING 신청 존재
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
                phoneNumber = "010-9100-0004",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "정원 가득 스터디",
                description = "정원 가득 스터디",
                capacity = 2,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        val approvedStudent = userRepository.save(
            User(
                trackId = track.trackId,
                email = "approved@gmail.com",
                name = "승인 교육생",
                phoneNumber = "010-9100-0005",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val pendingStudent = userRepository.save(
            User(
                trackId = track.trackId,
                email = "pending@gmail.com",
                name = "대기 교육생",
                phoneNumber = "010-9100-0006",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val pendingRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = pendingStudent.userId,
            studyId = studyId,
            appeal = "대기 신청"
        )
        val approvedRecruitmentId = studyRecruitmentService.requestRecruit(
            userId = approvedStudent.userId,
            studyId = studyId,
            appeal = "승인 신청"
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = approvedRecruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // when: 정원이 가득 찬 스터디에서 신청 승인 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.determineRecruit(
                leaderId = leader.userId,
                recruitmentId = pendingRecruitmentId,
                newStatus = RecruitStatus.APPROVED
            )
        }

        // then: 예외 발생(STUDY_CAPACITY_FULL)
        assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_FULL.code, thrown.code)
    }

    // ----- 신청 반려 테스트

    @Test
    @DisplayName("승인된 신청 건에 대해, 스터디장이 반려하는 경우, 예외 발생 - BusinessException(RECRUITMENT_INVALID_STATUS_CHANGE)")
    fun reject_after_approved_throws() {

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
                phoneNumber = "010-9200-0001",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "반려 테스트 스터디",
                description = "반려 테스트 스터디",
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
                phoneNumber = "010-9200-0002",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )
        studyRecruitmentService.determineRecruit(
            leaderId = leader.userId,
            recruitmentId = recruitmentId,
            newStatus = RecruitStatus.APPROVED
        )

        // when: 승인된 신청 건을 반려 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.determineRecruit(
                leaderId = leader.userId,
                recruitmentId = recruitmentId,
                newStatus = RecruitStatus.REJECTED
            )
        }

        // then: 예외 발생(RECRUITMENT_INVALID_STATUS_CHANGE)
        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    @DisplayName("취소된 신청 건에 대해, 스터디장이 반려하는 경우, 예외 발생 - BusinessException(RECRUITMENT_INVALID_STATUS_CHANGE)")
    fun reject_after_cancelled_throws() {

        /*
         * given
         * 1. ENROLLED 트랙 및 현재 차수 일정
         * 2. 모집 중(PENDING) 스터디
         * 3. 교육생 신청 후 취소
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
                phoneNumber = "010-9200-0003",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = leader.userId,
                name = "취소 반려 테스트 스터디",
                description = "취소 반려 테스트 스터디",
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
                phoneNumber = "010-9200-0004",
                provider = "GOOGLE",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE
            )
        )

        val recruitmentId = studyRecruitmentService.requestRecruit(
            userId = student.userId,
            studyId = studyId,
            appeal = "신청합니다."
        )
        studyRecruitmentService.cancelRecruit(
            userId = student.userId,
            recruitmentId = recruitmentId
        )

        // when: 취소된 신청 건을 반려 시도
        val thrown = assertThrows<BusinessException> {
            studyRecruitmentService.determineRecruit(
                leaderId = leader.userId,
                recruitmentId = recruitmentId,
                newStatus = RecruitStatus.REJECTED
            )
        }

        // then: 예외 발생(RECRUITMENT_INVALID_STATUS_CHANGE)
        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }
}
