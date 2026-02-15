package kr.co.wground.study.application

import java.time.LocalDate
import java.time.LocalTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.study_schedule.application.dto.ScheduleCreateCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study_schedule.domain.enums.Months
import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study_schedule.application.StudyScheduleService
import kr.co.wground.study_schedule.application.exception.StudyScheduleServiceErrorCode
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
@DisplayName("스터디 일정 서비스 테스트")
class StudyScheduleServiceTest {
    @Autowired
    private lateinit var studyScheduleService: StudyScheduleService

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    private lateinit var studyScheduleRepository: StudyScheduleRepository
    
    @Autowired
    private lateinit var studyRepository: StudyRepository

    @Test
    @DisplayName("일정 생성 테스트")
    fun shouldCreateSchedule_whenValidRequest() {

        // given
        val today = LocalDate.now()
        val trackInProgress = enrolledTrack(today)
        val savedTrack = trackRepository.save(trackInProgress)

        // when: 스터디 일정 생성
        // 특정 트랙의 1 개월 차 스터디 일정 생성
        val firstMonthScheduleRecruitStartAt = today.plusDays(1)
        val firstMonthScheduleRecruitEndAt = firstMonthScheduleRecruitStartAt.plusDays(5)
        val firstMonthScheduleStudyEndAt = firstMonthScheduleRecruitEndAt.plusDays(25)
        val firstMonthScheduleCreateCommand = ScheduleCreateCommand(
            trackId = savedTrack.trackId,
            month = Months.FIRST,
            recruitStartDate = firstMonthScheduleRecruitStartAt,
            recruitEndDate = firstMonthScheduleRecruitEndAt,
            studyEndDate = firstMonthScheduleStudyEndAt
        )
        val firstMonthScheduleCreateResponse = studyScheduleService.createSchedule(firstMonthScheduleCreateCommand)

        // 특정 트랙의 2 개월 차 스터디 일정 생성
        val secondMonthScheduleRecruitStartAt = firstMonthScheduleStudyEndAt.plusDays(1)
        val secondMonthScheduleRecruitEndAt = secondMonthScheduleRecruitStartAt.plusDays(5)
        val secondMonthScheduleStudyEndAt = secondMonthScheduleRecruitEndAt.plusDays(25)
        val secondMonthScheduleCreateCommand = ScheduleCreateCommand(
            trackId = savedTrack.trackId,
            month = Months.SECOND,
            recruitStartDate = secondMonthScheduleRecruitStartAt,
            recruitEndDate = secondMonthScheduleRecruitEndAt,
            studyEndDate = secondMonthScheduleStudyEndAt
        )
        val secondMonthScheduleCreateResponse = studyScheduleService.createSchedule(secondMonthScheduleCreateCommand)

        // 특정 트랙의 3 개월 차 스터디 일정 생성
        val thirdMonthScheduleRecruitStartAt = secondMonthScheduleStudyEndAt.plusDays(5)
        val thirdMonthScheduleRecruitEndAt = thirdMonthScheduleRecruitStartAt.plusDays(3)
        val thirdMonthScheduleStudyEndAt = thirdMonthScheduleRecruitEndAt.plusDays(26)
        val thirdMonthScheduleCreateCommand = ScheduleCreateCommand(
            trackId = savedTrack.trackId,
            month = Months.THIRD,
            recruitStartDate = thirdMonthScheduleRecruitStartAt,
            recruitEndDate = thirdMonthScheduleRecruitEndAt,
            studyEndDate = thirdMonthScheduleStudyEndAt
        )
        val thirdMonthScheduleCreateResponse = studyScheduleService.createSchedule(thirdMonthScheduleCreateCommand)

        // then:
        val savedSchedules = studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(savedTrack.trackId)
        assertEquals(3, savedSchedules.size)

        val firstSchedule = savedSchedules[0]
        assertNotNull(firstSchedule.id)
        assertEquals(savedTrack.trackId, firstSchedule.trackId)
        assertEquals(Months.FIRST, firstSchedule.months)
        assertEquals(firstMonthScheduleCreateCommand.recruitStartDate.atStartOfDay(), firstSchedule.recruitStartDate)
        assertEquals(firstMonthScheduleCreateCommand.recruitEndDate.atTime(LocalTime.MAX), firstSchedule.recruitEndDate)
        assertEquals(firstMonthScheduleCreateCommand.studyEndDate.atTime(LocalTime.MAX), firstSchedule.studyEndDate)

        val secondSchedule = savedSchedules[1]
        assertNotNull(secondSchedule.id)
        assertEquals(savedTrack.trackId, secondSchedule.trackId)
        assertEquals(Months.SECOND, secondSchedule.months)
        assertEquals(secondMonthScheduleCreateCommand.recruitStartDate.atStartOfDay(), secondSchedule.recruitStartDate)
        assertEquals(secondMonthScheduleCreateCommand.recruitEndDate.atTime(LocalTime.MAX), secondSchedule.recruitEndDate)
        assertEquals(secondMonthScheduleCreateCommand.studyEndDate.atTime(LocalTime.MAX), secondSchedule.studyEndDate)

        val thirdSchedule = savedSchedules[2]
        assertNotNull(thirdSchedule.id)
        assertEquals(savedTrack.trackId, thirdSchedule.trackId)
        assertEquals(Months.THIRD, thirdSchedule.months)
        assertEquals(thirdMonthScheduleCreateCommand.recruitStartDate.atStartOfDay(), thirdSchedule.recruitStartDate)
        assertEquals(thirdMonthScheduleCreateCommand.recruitEndDate.atTime(LocalTime.MAX), thirdSchedule.recruitEndDate)
        assertEquals(thirdMonthScheduleCreateCommand.studyEndDate.atTime(LocalTime.MAX), thirdSchedule.studyEndDate)
    }

    @Test
    @DisplayName("중복 트랙 및 차수 조합이 존재하는 경우 실패한다")
    fun shouldThrowDuplicateScheduleMonth_whenTrackMonthExists() {

        // given: 트랙 저장
        val today = LocalDate.now()
        val track = enrolledTrack(today)
        val savedTrack = trackRepository.save(track)

        val existingSchedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.FIRST,
            recruitStartDate = today.minusDays(10),
            recruitEndDate = today.minusDays(5),
            studyEndDate = today.plusDays(5)
        )
        studyScheduleRepository.save(existingSchedule)

        // when: 이미 저장된 일정과 동일한 트랙 및 차수를 가지는 일정 생성 시도 하면,
        // then: BusinessException 발생(DUPLICATE_SCHEDULE_MONTH)
        val thrown = assertThrows<BusinessException> {
            val command = ScheduleCreateCommand(
                trackId = savedTrack.trackId,
                month = Months.FIRST,
                recruitStartDate = today.minusDays(10),
                recruitEndDate = today.minusDays(5),
                studyEndDate = today.plusDays(5)
            )
            studyScheduleService.createSchedule(command)
        }

        assertEquals(StudyScheduleServiceErrorCode.DUPLICATE_SCHEDULE_MONTH.code, thrown.code)
    }

    // ----- 시점

    @Test
    @DisplayName("같은 트랙의 더 작은 차수 일정의 종료 시점이, 더 큰 차수 일정의 시작 시점보다 과거 시점이 아닌 경우 예외 발생 - BusinessException(SCHEDULE_OVERLAP_WITH_PREVIOUS)")
    fun shouldThrowScheduleOverlapWithPrevious_whenPreviousScheduleEndsAfterNextStarts() {

        // given: 트랙 저장 및 더 작은 차수 일정 저장
        val today = LocalDate.now()
        val track = enrolledTrack(today)
        val savedTrack = trackRepository.save(track)

        val recruitStartDateOfPreviousSchedule = today.minusDays(10)
        val recruitEndDateOfPreviousSchedule = today.minusDays(5)
        val studyEndDateOfPreviousSchedule = today.plusDays(10)
        val previousSchedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.FIRST,
            recruitStartDate = recruitStartDateOfPreviousSchedule,
            recruitEndDate = recruitEndDateOfPreviousSchedule,
            studyEndDate = studyEndDateOfPreviousSchedule
        )
        studyScheduleRepository.save(previousSchedule)

        // when: 더 큰 차수 일정의 모집 시작일이 이전 차수의 종료일보다 과거이거나 같으면
        // then: BusinessException 발생(SCHEDULE_OVERLAP_WITH_PREVIOUS)
        val thrown = assertThrows<BusinessException> {
            val command = ScheduleCreateCommand(
                trackId = savedTrack.trackId,
                month = Months.SECOND,
                recruitStartDate = studyEndDateOfPreviousSchedule.minusDays(60),
                recruitEndDate = studyEndDateOfPreviousSchedule.minusDays(55),
                studyEndDate = studyEndDateOfPreviousSchedule.minusDays(40)
            )
            studyScheduleService.createSchedule(command)
        }

        // To Do: 이 테스트의 에러는 일정이 겹치는 부분이 아니라 순서가 뒤바뀌었을 때의 규칙에 대한 것이므로 이름에 OVERLAP 이 포함된 것은 혼동을 줄 여지가 있음.
        // then: 이전 차수와 겹침 예외 발생
        assertEquals(StudyScheduleServiceErrorCode.SCHEDULE_OVERLAP_WITH_PREVIOUS.code, thrown.code)
    }

    @Test
    @DisplayName("같은 트랙의 서로 다른 임의의 두 차수의 일정이 서로 겹쳐지는 경우, 예외 발생 - BusinessException(SCHEDULE_OVERLAP_WITH_NEXT)")
    fun shouldThrowScheduleOverlapWithNext_whenSchedulesOverlapInSameTrack() {

        // given: 트랙 저장 및 더 큰 차수 일정 저장
        val today = LocalDate.now()
        val track = enrolledTrack(today)
        val savedTrack = trackRepository.save(track)

        val recruitStartDateOfNextSchedule = today.plusDays(10)
        val recruitEndDateOfNextSchedule = today.plusDays(12)
        val studyEndDateOfNextSchedule = today.plusDays(35)
        val nextSchedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.THIRD,
            recruitStartDate = recruitStartDateOfNextSchedule,
            recruitEndDate = recruitEndDateOfNextSchedule,
            studyEndDate = studyEndDateOfNextSchedule
        )
        studyScheduleRepository.save(nextSchedule)

        // when: 더 작은 차수 일정의 종료 시점이 더 큰 차수의 모집 시작 이후인 경우
        // then: BusinessException 발생(SCHEDULE_OVERLAP_WITH_NEXT)
        val thrown = assertThrows<BusinessException> {
            val command = ScheduleCreateCommand(
                trackId = savedTrack.trackId,
                month = Months.SECOND,
                recruitStartDate = studyEndDateOfNextSchedule.minusDays(1),
                recruitEndDate = studyEndDateOfNextSchedule.plusDays(2),
                studyEndDate = studyEndDateOfNextSchedule.plusDays(1)
            )
            studyScheduleService.createSchedule(command)
        }

        assertEquals(StudyScheduleServiceErrorCode.SCHEDULE_OVERLAP_WITH_NEXT.code, thrown.code)
    }

    // ----- Track

    @Test
    @DisplayName("존재하지 않는 trackId 를 이용한 일정 생성 시, 예외 발생 - BusinessException(TRACK_NOT_FOUND)")
    fun shouldThrowTrackNotFound_whenCreateScheduleWithMissingTrack() {

        // given: 존재하지 않는 trackId
        val today = LocalDate.now()
        val missingTrackId = 9999999L

        // when: 존재하지 않는 트랙으로 일정 생성 시도
        // then: BusinessException 발생(TRACK_NOT_FOUND)
        val thrown = assertThrows<BusinessException> {
            val command = ScheduleCreateCommand(
                trackId = missingTrackId,
                month = Months.FIRST,
                recruitStartDate = today.plusDays(1),
                recruitEndDate = today.plusDays(2),
                studyEndDate = today.plusDays(30)
            )
            studyScheduleService.createSchedule(command)
        }

        assertEquals(StudyServiceErrorCode.TRACK_NOT_FOUND.code, thrown.code)
    }

    @Test
    @DisplayName("졸업 트랙에 대해 종료되지 않은 일정 생성 시도 시, 예외 발생 - BusinessException(TRACK_IS_NOT_ENROLLED)")
    fun shouldThrowTrackIsNotEnrolled_whenCreateScheduleForGraduatedTrack() {
        
        // given: 이미 종료된(졸업) 트랙 저장
        val today = LocalDate.now()
        val graduatedTrack = Track(
            trackName = "졸업 트랙",
            startDate = today.minusDays(60),
            endDate = today.minusDays(1)     // 어제 끝 남 ㅂㅂ
        )
        val savedTrack = trackRepository.save(graduatedTrack)

        // when: 종료되지 않은 일정 생성 시도
        // then: BusinessException 발생(TRACK_IS_NOT_ENROLLED)
        val thrown = assertThrows<BusinessException> {
            val command = ScheduleCreateCommand(
                trackId = savedTrack.trackId,
                month = Months.FIRST,
                recruitStartDate = today.plusDays(1),
                recruitEndDate = today.plusDays(2),
                studyEndDate = today.plusDays(30)
            )
            studyScheduleService.createSchedule(command)
        }

        assertEquals(StudyServiceErrorCode.TRACK_IS_NOT_ENROLLED.code, thrown.code)
    }

    // ----- Study

    @Test
    @DisplayName("일정을 따르는 스터디가 존재할 때, 해당 일정을 삭제 시도하면, 예외 발생 - BusinessException(CANNOT_DELETE_SCHEDULE_WITH_STUDIES)")
    fun shouldThrowCannotDeleteScheduleWithStudies_whenDeletingScheduleWithStudies() {

        // given: 트랙/일정 저장 및 해당 일정을 따르는 스터디 저장
        val today = LocalDate.now()
        val track = enrolledTrack(today)
        val savedTrack = trackRepository.save(track)

        val schedule = StudySchedule(
            trackId = savedTrack.trackId,
            months = Months.FIRST,
            recruitStartDate = today.minusDays(10),
            recruitEndDate = today.minusDays(5),
            studyEndDate = today.plusDays(20)
        )
        val savedSchedule = studyScheduleRepository.save(schedule)

        val study = Study.createNew(
            name = "스터디 이름",
            leaderId = 1L,
            trackId = savedTrack.trackId,
            scheduleId = savedSchedule.id,
            description = "스터디 설명",
            capacity = 5,
            budget = BudgetType.MEAL,
            budgetExplain = "🍕🍕🍕",
        )
        studyRepository.save(study)

        // when: 해당 일정을 삭제 시도
        // then: BusinessException 발생(CANNOT_DELETE_SCHEDULE_WITH_STUDIES)
        val thrown = assertThrows<BusinessException> {
            studyScheduleService.deleteSchedule(savedSchedule.id)
        }

        assertEquals(StudyScheduleServiceErrorCode.CANNOT_DELETE_SCHEDULE_WITH_STUDIES.code, thrown.code)
    }

    // ----- helpers

    private fun enrolledTrack(today: LocalDate): Track {
        return Track(
            trackName = "테스트 트랙",
            startDate = today.minusDays(30),
            endDate = today.plusDays(30)
        )
    }
}

