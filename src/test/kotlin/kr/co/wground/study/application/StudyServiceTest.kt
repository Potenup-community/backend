package kr.co.wground.study.application

import jakarta.persistence.EntityManager
import java.time.LocalDate
import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.application.dto.StudyUpdateCommand
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
            chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
            refUrl = null,
            tags = emptyList()
        )
        val studyId = studyService.createStudy(command)

        // then: 반드시 자신이 생성한 스터디에 참여된 상태여야 한다.
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(1, recruitments.size)

        val leaderRecruitment = recruitments.first()
        assertEquals(savedUser.userId, leaderRecruitment.userId)
        assertEquals(RecruitStatus.APPROVED, leaderRecruitment.recruitStatus)
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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
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
            chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
            refUrl = null,
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
                chatUrl = null,
                refUrl = null,
                tags = updateTags,
                scheduleId = schedule.id
            )
            studyService.updateStudy(command)
        }

        assertEquals(StudyDomainErrorCode.STUDY_TAG_COUNT_EXCEEDED.code, thrown.code)
    }

    // ----- 거부 테스트

    /*
     * [참고]
     * - 테스트에서 별도로 반려시키는 게 아니라, 스터디 생성 후 자동으로 참여된 상태임을 확인해야 하는 테스트임
     * - 스터디장의 신청 상태 역시 반려 상태로 바뀌었는 지 확인함. 왜냐하면, 반려된 테스트에 승인 상태로 남아있으면
     *   스터디 참여 개수를 점유하기 때문임
     */
    @Test
    @DisplayName("스터디가 거부된 경우, CANCELLED 상태가 아닌 모든 신청 건이 반려(REJECTED)된다")
    fun shouldRejectNonCancelledRecruitments_whenStudyRejected() {
        /*
         * given
         * 1. 스터디가 존재한다.
         * 2. PENDING/APPROVED/CANCELLED/REJECTED 신청이 함께 존재한다.
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val leader = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-3333-3333",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        // 스터디를 생성할 때 서비스 메서드를 통해 생성하여, 스터디장의 자동으로 생성된 스터디에 참여되도록 함
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "거부 테스트 스터디",
                description = "거부 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val studentPending = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "PENDING 상태 학생",
            phoneNumber = "010-1111-1111",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val pendingRecruitment = StudyRecruitment(
            userId = studentPending.userId,
            study = savedStudy!!,
            appeal = "신청",
            recruitStatus = RecruitStatus.PENDING
        )
        val studentApproved = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "APPROVED 상태 학생",
            phoneNumber = "010-1111-1111",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val approvedRecruitment = StudyRecruitment(
            userId = studentApproved.userId,
            study = savedStudy,
            appeal = "승인",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now()
        )
        val studentCancelled = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "CANCELLED 상태 학생",
            phoneNumber = "010-1111-1111",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val cancelledRecruitment = StudyRecruitment(
            userId = studentCancelled.userId,
            study = savedStudy,
            appeal = "취소",
            recruitStatus = RecruitStatus.CANCELLED
        )
        val studentRejected = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "REJECTED 상태 학생",
            phoneNumber = "010-1111-1111",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val rejectedRecruitment = StudyRecruitment(
            userId = studentRejected.userId,
            study = savedStudy,
            appeal = "반려",
            recruitStatus = RecruitStatus.REJECTED
        )
        studyRecruitmentRepository.saveAll(
            listOf(pendingRecruitment, approvedRecruitment, cancelledRecruitment, rejectedRecruitment)
        )

        // when: 스터디 거부 처리
        studyService.rejectStudy(savedStudy.id)

        // then: CANCELLED를 제외한 모든 신청이 REJECTED로 변경
        entityManager.flush()
        entityManager.clear()
        val updated = studyRecruitmentRepository.findAllByStudyId(savedStudy.id)
            .associateBy { it.userId }

        assertEquals(RecruitStatus.REJECTED, updated[savedLeader.userId]?.recruitStatus)
        assertEquals(RecruitStatus.REJECTED, updated[studentPending.userId]?.recruitStatus)
        assertEquals(RecruitStatus.REJECTED, updated[studentApproved.userId]?.recruitStatus)
        assertEquals(RecruitStatus.CANCELLED, updated[studentCancelled.userId]?.recruitStatus)
        assertEquals(RecruitStatus.REJECTED, updated[studentRejected.userId]?.recruitStatus)
    }

    // ----- 결재 테스트

    @Test
    @DisplayName("스터디가 결재된 경우, 관련된 모든 신청 건 중 PENDING 상태인 신청만 REJECTED 상태로 변경되어야 한다")
    fun shouldRejectPendingRecruitments_whenStudyApproved() {

        /*
         * given
         * 1. 스터디 생성(스터디장은 자동 참여)
         * 2. PENDING/APPROVED/CANCELLED/REJECTED 신청이 함께 존재한다.
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val leader = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-4444-4444",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "결재 테스트 스터디",
                description = "결재 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        // 스터디 결재 조건 충족: 모집 마감 + 최소 인원 이상
        savedStudy!!.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = today.minusDays(10),
            newRecruitEnd = today.minusDays(5),
            newStudyEnd = today.plusDays(10)
        )
        savedStudy.refreshStatus(schedule.isRecruitmentClosed())

        val pendingRecruitment = StudyRecruitment(
            userId = 2L,
            study = savedStudy,
            appeal = "신청",
            recruitStatus = RecruitStatus.PENDING
        )
        val approvedRecruitment = StudyRecruitment(
            userId = 3L,
            study = savedStudy,
            appeal = "승인",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now()
        )
        val cancelledRecruitment = StudyRecruitment(
            userId = 4L,
            study = savedStudy,
            appeal = "취소",
            recruitStatus = RecruitStatus.CANCELLED
        )
        val rejectedRecruitment = StudyRecruitment(
            userId = 5L,
            study = savedStudy,
            appeal = "반려",
            recruitStatus = RecruitStatus.REJECTED
        )
        studyRecruitmentRepository.saveAll(
            listOf(pendingRecruitment, approvedRecruitment, cancelledRecruitment, rejectedRecruitment)
        )

        // when: 스터디 결재 처리
        studyService.approveStudy(savedStudy.id)

        // then: PENDING 상태인 신청은 REJECTED로 변경
        entityManager.flush()
        entityManager.clear()
        val updated = studyRecruitmentRepository.findAllByStudyId(savedStudy.id)
            .associateBy { it.userId }

        assertEquals(RecruitStatus.APPROVED, updated[savedLeader.userId]?.recruitStatus)
        assertEquals(RecruitStatus.REJECTED, updated[2L]?.recruitStatus)
        assertEquals(RecruitStatus.APPROVED, updated[3L]?.recruitStatus)
        assertEquals(RecruitStatus.CANCELLED, updated[4L]?.recruitStatus)
        assertEquals(RecruitStatus.REJECTED, updated[5L]?.recruitStatus)
    }

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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val extraRecruitment = StudyRecruitment(
            userId = 2L,
            study = savedStudy!!,
            appeal = "추가 신청",
            recruitStatus = RecruitStatus.PENDING
        )
        studyRecruitmentRepository.save(extraRecruitment)

        entityManager.flush()
        entityManager.clear()
        val recruitmentsBefore = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(2, recruitmentsBefore.size)

        // when: 스터디 삭제
        studyService.deleteStudy(studyId, savedLeader.userId, isAdmin = false)

        // then: 스터디 및 관련 신청건 삭제
        entityManager.flush()
        entityManager.clear()
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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val extraRecruitment = StudyRecruitment(
            userId = 2L,
            study = savedStudy!!,
            appeal = "추가 신청",
            recruitStatus = RecruitStatus.PENDING
        )
        studyRecruitmentRepository.save(extraRecruitment)

        entityManager.flush()
        entityManager.clear()
        val recruitmentsBefore = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(2, recruitmentsBefore.size)

        // 모집 마감 + 최소 인원 충족 -> CLOSED
        val managedStudy = studyRepository.findByIdOrNull(studyId)!!
        managedStudy.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = today.minusDays(10),
            newRecruitEnd = today.minusDays(5),
            newStudyEnd = today.plusDays(10)
        )
        managedStudy.refreshStatus(schedule.isRecruitmentClosed())

        // when: 스터디 삭제
        studyService.deleteStudy(studyId, savedLeader.userId, isAdmin = false)

        // then: 스터디 및 관련 신청건 삭제
        entityManager.flush()
        entityManager.clear()
        val deletedStudy = studyRepository.findByIdOrNull(studyId)
        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)

        assertEquals(null, deletedStudy)
        assertEquals(0, recruitments.size)
    }

    @Test
    @DisplayName("거부(REJECTED) 상태의 스터디를 삭제하려 한 경우, 예외 발생 - BusinessException(STUDY_CANT_DELETE_STATUS_DETERMINE)")
    fun shouldThrowStudyCantDeleteStatusDetermine_whenDeleteRejectedStudy() {

        /*
         * given
         * 1. REJECTED 상태의 스터디
         */
        val today = LocalDate.now()
        val savedTrack = createAndSaveEnrolledTrack(today)

        val schedule = createAndSaveCurrentSchedule(today, savedTrack)

        val leader = User(
            trackId = savedTrack.trackId,
            email = "test@gmail.com",
            name = "스터디장",
            phoneNumber = "010-7777-7777",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )
        val savedLeader = userRepository.save(leader)

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "삭제 테스트 스터디(REJECTED)",
                description = "삭제 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        savedStudy!!.reject()

        // when: REJECTED 스터디 삭제 시도
        val thrown = assertThrows<BusinessException> {
            studyService.deleteStudy(savedStudy.id, savedLeader.userId, isAdmin = false)
        }

        // then: 예외 발생(STUDY_CANT_DELETE_STATUS_DETERMINE)
        assertEquals(StudyDomainErrorCode.STUDY_CANT_DELETE_STATUS_DETERMINE.code, thrown.code)
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

        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedLeader.userId,
                name = "삭제 테스트 스터디(APPROVED)",
                description = "삭제 테스트",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        // 모집 마감 + 최소 인원 충족 -> CLOSED -> APPROVED
        savedStudy!!.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
        schedule.updateSchedule(
            newMonths = null,
            newRecruitStart = today.minusDays(10),
            newRecruitEnd = today.minusDays(5),
            newStudyEnd = today.plusDays(10)
        )
        savedStudy.refreshStatus(schedule.isRecruitmentClosed())
        savedStudy.approve()
        assertEquals(StudyStatus.APPROVED, savedStudy.status)

        // when: APPROVED 스터디 삭제 시도
        val thrown = assertThrows<BusinessException> {
            studyService.deleteStudy(savedStudy.id, savedLeader.userId, isAdmin = false)
        }

        // then: 예외 발생(STUDY_CANT_DELETE_STATUS_DETERMINE)
        assertEquals(StudyDomainErrorCode.STUDY_CANT_DELETE_STATUS_DETERMINE.code, thrown.code)
    }

    // ----- 참여 스터디 수 제한 테스트

    // To Do: 현재 차수 스터디에 2개 생성한 상태에서, 한 번 더 생성 시 예외 발생 - BusinessException

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 모두 PENDING 상태일 때, 신규 스터디 생성 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
    fun shouldThrowMaxStudyExceeded_whenTwoPendingApplicationsExist() {

        /*
         * given
         * 1. ENROLLED 상태의 트랙
         * 2. 과거 차수 일정/스터디 참여 이력
         * 3. 현재 차수 스터디 2개에 PENDING 신청
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
        val pastStudy = Study(
            name = "과거 차수 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedPastSchedule.id,
            description = "과거 차수 참여",
            status = StudyStatus.PENDING,
            capacity = 5,
            budget = BudgetType.MEAL
        )
        val savedPastStudy = studyRepository.save(pastStudy)
        val pastRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = savedPastStudy,
            appeal = "과거 참여",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now().minusDays(30)
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

        val currentStudy1 = studyRepository.findByIdOrNull(studyId1)!!
        val currentStudy2 = studyRepository.findByIdOrNull(studyId2)!!

        val pendingRecruitment1 = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy1,
            appeal = "신청1",
            recruitStatus = RecruitStatus.PENDING
        )
        val pendingRecruitment2 = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy2,
            appeal = "신청2",
            recruitStatus = RecruitStatus.PENDING
        )
        studyRecruitmentRepository.saveAll(listOf(pendingRecruitment1, pendingRecruitment2))

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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
            studyService.createStudy(command)
        }

        // then: 예외 발생(MAX_STUDY_EXCEEDED)
        assertEquals(StudyServiceErrorCode.MAX_STUDY_EXCEEDED.code, thrown.code)
    }

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 모두 APPROVED 상태일 때, 신규 스터디 생성 시 예외 발생 - BusinessException(MAX_STUDY_EXCEEDED)")
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
        val pastStudy = Study(
            name = "과거 차수 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedPastSchedule.id,
            description = "과거 차수 참여",
            status = StudyStatus.PENDING,
            capacity = 5,
            budget = BudgetType.MEAL
        )
        val savedPastStudy = studyRepository.save(pastStudy)
        val pastRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = savedPastStudy,
            appeal = "과거 참여",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now().minusDays(30)
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

        val currentStudy1 = studyRepository.findByIdOrNull(studyId1)!!
        val currentStudy2 = studyRepository.findByIdOrNull(studyId2)!!

        val approvedRecruitment1 = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy1,
            appeal = "승인1",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now()
        )
        val approvedRecruitment2 = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy2,
            appeal = "승인2",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now()
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
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
            studyService.createStudy(command)
        }

        // then: 예외 발생(MAX_STUDY_EXCEEDED)
        assertEquals(StudyServiceErrorCode.MAX_STUDY_EXCEEDED.code, thrown.code)
    }

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 중 하나는 PENDING 상태, 다른 하나는 REJECTED 상태 인 경우, 신규 스터디 생성이 가능하다")
    fun shouldAllowStudyCreation_whenPendingAndRejectedApplicationsExist() {

        /*
         * given
         * 1. ENROLLED 상태의 트랙
         * 2. 과거 차수 일정/스터디 참여 이력
         * 3. 현재 차수 스터디 2개에 PENDING/REJECTED 신청
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
        val pastStudy = Study(
            name = "과거 차수 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedPastSchedule.id,
            description = "과거 차수 참여",
            status = StudyStatus.PENDING,
            capacity = 5,
            budget = BudgetType.MEAL
        )
        val savedPastStudy = studyRepository.save(pastStudy)
        val pastRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = savedPastStudy,
            appeal = "과거 참여",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now().minusDays(30)
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

        val currentStudy1 = studyRepository.findByIdOrNull(studyId1)!!
        val currentStudy2 = studyRepository.findByIdOrNull(studyId2)!!

        val pendingRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy1,
            appeal = "신청",
            recruitStatus = RecruitStatus.PENDING
        )
        val rejectedRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy2,
            appeal = "반려",
            recruitStatus = RecruitStatus.REJECTED
        )
        studyRecruitmentRepository.saveAll(listOf(pendingRecruitment, rejectedRecruitment))

        entityManager.flush()
        entityManager.clear()

        // when: 신규 스터디 생성
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedStudent.userId,
                name = "추가 스터디",
                description = "추가 스터디",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        // then: 신규 스터디 생성 성공
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(1, recruitments.size)
        assertEquals(savedStudent.userId, recruitments.first().userId)
        assertEquals(RecruitStatus.APPROVED, recruitments.first().recruitStatus)
    }

    @Test
    @DisplayName("과거 차수에 대한 스터디 참여 이력이 있는, 특정 트랙의 교육생이, 해당 트랙의 서로 다른 현재 차수 스터디에 두 개 신청했으며, 두 신청 건 중 하나는 PENDING 상태, 다른 하나는 CANCELLED 상태 인 경우, 신규 스터디 생성이 가능하다")
    fun shouldAllowStudyCreation_whenPendingAndCancelledApplicationsExist() {

        /*
         * given
         * 1. ENROLLED 상태의 트랙
         * 2. 과거 차수 일정/스터디 참여 이력
         * 3. 현재 차수 스터디 2개에 PENDING/CANCELLED 신청
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
        val pastStudy = Study(
            name = "과거 차수 스터디",
            leaderId = 10L,
            trackId = savedTrack.trackId,
            scheduleId = savedPastSchedule.id,
            description = "과거 차수 참여",
            status = StudyStatus.PENDING,
            capacity = 5,
            budget = BudgetType.MEAL
        )
        val savedPastStudy = studyRepository.save(pastStudy)
        val pastRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = savedPastStudy,
            appeal = "과거 참여",
            recruitStatus = RecruitStatus.APPROVED,
            approvedAt = LocalDateTime.now().minusDays(30)
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

        val currentStudy1 = studyRepository.findByIdOrNull(studyId1)!!
        val currentStudy2 = studyRepository.findByIdOrNull(studyId2)!!

        val pendingRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy1,
            appeal = "신청",
            recruitStatus = RecruitStatus.PENDING
        )
        val cancelledRecruitment = StudyRecruitment(
            userId = savedStudent.userId,
            study = currentStudy2,
            appeal = "취소",
            recruitStatus = RecruitStatus.CANCELLED
        )
        studyRecruitmentRepository.saveAll(listOf(pendingRecruitment, cancelledRecruitment))

        entityManager.flush()
        entityManager.clear()

        // when: 신규 스터디 생성
        val studyId = studyService.createStudy(
            StudyCreateCommand(
                userId = savedStudent.userId,
                name = "추가 스터디",
                description = "추가 스터디",
                capacity = 5,
                budget = BudgetType.MEAL,
                chatUrl = "https://www.kakaocorp.com/page/service/service/openchat",
                refUrl = null,
                tags = emptyList()
            )
        )

        // then: 신규 스터디 생성 성공
        val savedStudy = studyRepository.findByIdOrNull(studyId)
        assertNotNull(savedStudy)

        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)
        assertEquals(1, recruitments.size)
        assertEquals(savedStudent.userId, recruitments.first().userId)
        assertEquals(RecruitStatus.APPROVED, recruitments.first().recruitStatus)
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
