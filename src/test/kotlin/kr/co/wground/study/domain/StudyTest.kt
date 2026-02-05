package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.Months
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.repeat

@DisplayName("스터디(Study) 테스트")
class StudyTest {

    // To Do: 특정 값을 변경하고 싶지 않은 경우, null 을 명시적으로 전달하는 방식이 괜찮을 지 모르겠음
    val NOT_GONNA_CHANGE = null;

    companion object {

        @JvmStatic
        fun invalidCapacities(): Stream<Arguments> = Stream.of(
            Arguments.of("too small(${Study.MIN_CAPACITY} 미만)", Study.MIN_CAPACITY - 1, StudyDomainErrorCode.STUDY_CAPACITY_TOO_SMALL.code),
            Arguments.of("too big(${Study.ABSOLUTE_MAX_CAPACITY} 초과)", Study.ABSOLUTE_MAX_CAPACITY + 1, StudyDomainErrorCode.STUDY_CAPACITY_TOO_BIG.code)
        )

        @JvmStatic
        fun invalidNames(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", ""),
            Arguments.of("blank(space)", " "),
            Arguments.of("blank(tab)", "\t"),
            Arguments.of("blank(newline)", "\n"),
            Arguments.of("blank(mixed)", " \t \n "),
            Arguments.of("too short(${Study.MIN_NAME_LENGTH}자 미만)", "*".repeat(Study.MIN_NAME_LENGTH - 1)),
            Arguments.of("too short(${Study.MIN_NAME_LENGTH}자 미만; trimmed)", " 1 "),
            Arguments.of("too short(${Study.MIN_NAME_LENGTH}자 미만; mixed)", " \t1\n "),
            Arguments.of("too long(${Study.MAX_NAME_LENGTH}자 초과)", " \t" + "*".repeat(Study.MAX_NAME_LENGTH + 1) + "\n ")
        )

        @JvmStatic
        fun invalidDescriptions(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", ""),
            Arguments.of("blank(space)", " "),
            Arguments.of("blank(tab)", "\t"),
            Arguments.of("blank(newline)", "\n"),
            Arguments.of("blank(mixed)", " \t \n "),
            Arguments.of("too short(${Study.MIN_DESCRIPTION_LENGTH}자 미만)", "*".repeat(Study.MIN_DESCRIPTION_LENGTH - 1)),
            Arguments.of("too short(${Study.MIN_DESCRIPTION_LENGTH}자 미만; trimmed)", " 1 "),
            Arguments.of("too short(${Study.MIN_DESCRIPTION_LENGTH}자 미만; mixed)", " \t1\n "),
            Arguments.of("too long(${Study.MAX_DESCRIPTION_LENGTH}자 초과)", " \t" + "*".repeat(Study.MAX_DESCRIPTION_LENGTH + 1) + "\n ")
        )
    }

    // ----- 정원 수

    @ParameterizedTest(name = "정원 수: {0}")
    @MethodSource("invalidCapacities")
    @DisplayName("스터디 생성 시, 정원 수가 MIN_CAPACITY 미만이거나 ABSOLUTE_MAX_CAPACITY 초과이면, 예외 발생")
    fun shouldThrowBusinessException_whenCreateStudyWithInvalidCapacity(caseName: String, givenCapacity: Int, expectedErrorCode: String) {

        // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
        val thrown = assertThrows<BusinessException> {
            createStudyWithCapacity(createRecruitingStudySchedule(), givenCapacity)
        }

        assertEquals(expectedErrorCode, thrown.code)
    }

    @ParameterizedTest(name = "정원 수: {0}")
    @MethodSource("invalidCapacities")
    @DisplayName("스터디 수정 시, 정원 수가 MIN_CAPACITY 미만이거나 ABSOLUTE_MAX_CAPACITY 초과이면, 예외 발생")
    fun shouldThrowStudyCapacityTooSmall_whenUpdateStudyWithInvalidCapacity(caseName: String, givenCapacity: Int, expectedErrorCode: String) {
        // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val validCapacity = Study.MIN_CAPACITY
            val created = createStudyWithCapacity(schedule, validCapacity)
            updateStudyCapacity(
                created, givenCapacity, schedule.isRecruitmentClosed())
        }

        assertEquals(expectedErrorCode, thrown.code)
    }

    // ----- 제목

    @ParameterizedTest(name = "스터디 제목: {0}")
    @MethodSource("invalidNames")
    @DisplayName("스터디 생성 시, 앞뒤 공백 제거 기준, 제목이 유효하지 않은 경우, 예외 발생 - BusinessException(STUDY_NAME_INVALID)")
    fun shouldThrowStudyNameInvalid_whenCreateStudyWithInvalidTitle(caseName: String, givenTitle: String) {

        val thrown = assertThrows<BusinessException> {
            createStudyWithName(createRecruitingStudySchedule(), givenTitle)
        }

        assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
    }

    @ParameterizedTest(name = "스터디 제목: {0}")
    @MethodSource("invalidNames")
    @DisplayName("스터디 수정 시, 앞뒤 공백 제거 기준, 제목이 유효하지 않은 경우, 예외 발생 - BusinessException(STUDY_NAME_INVALID)")
    fun shouldThrowStudyNameInvalid_whenUpdateStudyWithInvalidTitle(caseName: String, givenTitle: String) {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val validTitle: String = "유효한 제목"
            val created = createStudyWithName(schedule, validTitle)

            updateStudyName(created, givenTitle, schedule.isRecruitmentClosed())
        }

        assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
    }

    // ----- 소개 글

    @ParameterizedTest(name = "스터디 소개글: {0}")
    @MethodSource("invalidDescriptions")
    @DisplayName("스터디 생성 시, 앞뒤 공백 제거 기준, 소개글이 유효하지 않은 경우, 예외 발생 - BusinessException(STUDY_DESCRIPTION_INVALID)")
    fun shouldThrowStudyDescriptionInvalid_whenCreateStudyWithInvalidDescription(caseName: String, givenDescription: String) {
        val thrown = assertThrows<BusinessException> {
            createStudyWithDescription(createRecruitingStudySchedule(), givenDescription)
        }

        assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
    }

    @ParameterizedTest(name = "스터디 소개글: {0}")
    @MethodSource("invalidDescriptions")
    @DisplayName("스터디 수정 시, 앞뒤 공백 제거 기준, 소개글이 유효하지 않은 경우, 예외 발생 - BusinessException(STUDY_DESCRIPTION_INVALID)")
    fun shouldThrowStudyDescriptionInvalid_whenUpdateStudyWithInvalidDescription(caseName: String, givenDescription: String) {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val validDescription = "유효한 소개글"
            val created = createStudyWithDescription(schedule, validDescription)

            updateStudyDescription(
                created, givenDescription, schedule.isRecruitmentClosed())
        }

        assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
    }

    // ----- 채팅 방 링크

    @Test
    @DisplayName("스터디 생성 시, 채팅 방 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(STUDY_URL_INVALID)")
    fun shouldThrowStudyUrlInvalid_whenCreateStudyWithInvalidChatUrl() {
        val thrown = assertThrows<BusinessException> {
            createStudyWithExternalChatUrl(
                createRecruitingStudySchedule(), "유효하지 않은 형식의 링크")
        }

        assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
    }

    @Test
    @DisplayName("스터디 수정 시, 채팅 방 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(STUDY_URL_INVALID)")
    fun shouldThrowStudyUrlInvalid_whenUpdateStudyWithInvalidChatUrl() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithExternalChatUrl(
                schedule, "https://www.kakaocorp.com/page/service/service/openchat")

            updateStudyExternalChatUrl(
                created,
                "유효하지 않은 형식의 링크",
                schedule.isRecruitmentClosed()
            )
        }

        assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
    }

    // ----- 참고 자료 링크

    @Test
    @DisplayName("스터디 생성 시, 참고 자료 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(STUDY_URL_INVALID)")
    fun shouldThrowStudyUrlInvalid_whenCreateStudyWithInvalidRefUrl() {
        val thrown = assertThrows<BusinessException> {
            createStudyWithReferenceUrl(
                createRecruitingStudySchedule(), "유효하지 않은 형식의 링크")
        }

        assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
    }

    @Test
    @DisplayName("스터디 수정 시, 참고 자료 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(STUDY_URL_INVALID)")
    fun shouldThrowStudyUrlInvalid_whenUpdateStudyWithInvalidRefUrl() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithReferenceUrl(
                schedule, "https://www.kakaocorp.com/page/service/service/openchat")

            updateStudyReferenceUrl(
                created,
                "유효하지 않은 형식의 링크",
                schedule.isRecruitmentClosed()
            )
        }

        assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
    }

    // ----- 마감 테스트

    // To Do: 모집 마김 일자가 지나기 전에 마감 시도한 경우 예외 발생 - BusinessException(RECRUITMENT_NOT_ENDED_YET)

    // ----- 결재 테스트

    @Test
    @DisplayName("대상 스터디가 PENDING 상태일 때, 스터디 결재 시, 예외 발생 - BusinessException(STUDY_MUST_BE_CLOSED_TO_APPROVE)")
    fun shouldThrowStudyMustBeClosedToApprove_whenApprovePendingStudy() {

        val thrown = assertThrows<BusinessException> {
            val pending = createStudyWithCapacity(createRecruitingStudySchedule(),2)
            pending.approve()
        }

        assertEquals(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE.code, thrown.code)
    }

    @Test
    @DisplayName("스터디가 CLOSED 상태이고 최소 인원에 미달되었을 때, 결재 시도한 경우, 예외 발생 - BusinessException(STUDY_CANNOT_APPROVED_DUE_TO_NOT_ENOUGH_MEMBER)")
    // STUDY_CANNOT_APPROVED_DUE_TO_NOT_ENOUGH_MEMBER
    fun shouldThrowStudyCannotApprovedDueToNotEnoughMember_whenApproveStudyWhichNotMeetMinimumMemberCondition() {

        val thrown = assertThrows<BusinessException> {
            val pending = createStudyWithCapacity(createRecruitingStudySchedule(), Study.MIN_CAPACITY)
            pending.close(LocalDateTime.now().minusDays(1))

            pending.approve()
        }

        assertEquals(StudyDomainErrorCode.STUDY_CANNOT_APPROVED_DUE_TO_NOT_ENOUGH_MEMBER.code, thrown.code)
    }

    // ----- 참여 인원 수 테스트 (스터디 신청과의 정합성 고려 x)

    @Test
    @DisplayName("APPROVED 상태에서, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(STUDY_NOT_RECRUITING)")
    fun shouldThrowStudyNotRecruiting_whenIncreaseMemberOnApprovedStudy() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, 3)
            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())

            // To Do: 실제 사용 시 이런 식으로 사용하면 안 되나, 테스트를 위해 의도적으로 과거 시점을 전달함
            created.close(LocalDateTime.now().minusDays(1))
            created.approve()

            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
        }

        assertEquals(StudyDomainErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
    }

    @Test
    @DisplayName("이미 정원이 가득 찬 경우, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(STUDY_CAPACITY_FULL)")
    fun shouldThrowStudyCapacityFull_whenIncreaseMemberAtCapacity() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)
            repeat(Study.MIN_CAPACITY - 1) {
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            }
            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
        }

        assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_FULL.code, thrown.code)
    }

    @Test
    @DisplayName("이미 모집 기간이 마감된 경우(스터디 상태가 CLOSED 일 때), 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(STUDY_ALREADY_FINISH_TO_RECRUIT)")
    fun shouldThrowStudyAlreadyFinishToRecruit_whenIncreaseMemberAfterRecruitEnd() {
        val thrown = assertThrows<BusinessException> {
            val alreadyStartedSchedule = createAlreadyStartedStudySchedule()
            val created = createStudyWithName(alreadyStartedSchedule, "유효한 이름")

            created.increaseMemberCount(
                alreadyStartedSchedule.recruitEndDate,
                alreadyStartedSchedule.isRecruitmentClosed()
            )
        }

        assertEquals(StudyDomainErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT.code, thrown.code)
    }

    // ----- 수정 테스트

    @Test
    @DisplayName("APPROVED 상태인 경우, 스터디를 수정 시, 예외 발생 - BusinessException(STUDY_CANNOT_MODIFY_AFTER_DETERMINED)")
    fun shouldThrowStudyCannotModifyAfterDetermined_whenUpdateApprovedStudy() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)
            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            created.approve()

            updateStudyName(created, "제목제목", schedule.isRecruitmentClosed())
        }

        assertEquals(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_APPROVED.code, thrown.code)
    }

    @Test
    @DisplayName("PENDING 상태일 때, 생성 시점에 설정 가능한 임의의 항목을 수정할 시, 성공한다")
    fun shouldUpdateEditableFields_whenStudyPending() {
        val schedule = createRecruitingStudySchedule()
        val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)

        val newCapacity = created.capacity + 1
        val newName = created.name + "a"
        val newDescription = created.description + "a"
        val newBudget = if (created.budget == BudgetType.BOOK) BudgetType.MEAL else BudgetType.BOOK
        val newChatUrl = created.externalChatUrl + "a"
        val newRefUrl =
            if (created.referenceUrl == null) "https://tecoble.techcourse.co.kr/" else created.referenceUrl + "a"

        val tagSizeBefore = created.studyTags.size
        val newTags = created.studyTags
            .map { studyTag -> studyTag.tag }
            .toMutableList()
        newTags.add(Tag.create("아마도새로운태그일걸요1"))

        created.updateStudyInfo(
            newCapacity = newCapacity,
            newName = newName,
            newDescription = newDescription,
            newBudget = newBudget,
            newChatUrl = newChatUrl,
            newRefUrl = newRefUrl,
            newTags = newTags,

            newScheduleId = created.scheduleId,
            isRecruitmentClosed = schedule.isRecruitmentClosed(),
        )

        // 문제 사유 addTag 내부에서 Tag 존재 여부를 id 로 구분하고 있으나, 새로 생성된 태그들은 모두 id 가 0 이어서 구분되지 않음.
        created.addTag(Tag.create("아마도새로운태그일걸요2"))

        assertAll(
            { assertEquals(newCapacity, created.capacity) },
            { assertEquals(newName, created.name) },
            { assertEquals(newDescription, created.description) },
            { assertEquals(newBudget, created.budget) },
            { assertEquals(newChatUrl, created.externalChatUrl) },
            { assertEquals(newRefUrl, created.referenceUrl) },
            { assertTrue { 1 == created.studyTags.filter { studyTag -> studyTag.tag.name.contains("아마도새로운태그일걸요1") }.size } },
            { assertTrue { 1 == created.studyTags.filter { studyTag -> studyTag.tag.name.contains("아마도새로운태그일걸요2") }.size } }
        )
    }

    // ----- factories for test

    private fun createRecruitingStudySchedule(): StudySchedule {
        return StudySchedule(
            trackId = 3L,
            months = Months.FIRST,
            recruitStartDate = LocalDate.now().minusDays(4),
            recruitEndDate = LocalDate.now().plusDays(3),
            studyEndDate = LocalDate.now().plusDays(26)
        )
    }

    private fun createAlreadyStartedStudySchedule(): StudySchedule {
        return StudySchedule(
            trackId = 3L,
            months = Months.THIRD,
            recruitStartDate = LocalDate.now().minusDays(7),
            recruitEndDate = LocalDate.now().minusDays(3),
            studyEndDate = LocalDate.now().plusDays(21)
        )
    }

    private fun createStudyWithCapacity(schedule: StudySchedule, capacity: Int): Study {
        return Study(
            capacity = capacity,
            budget = BudgetType.BOOK,
            name = "스터디 제목",
            description = "스터디 소개글",
            leaderId = 1L,
            trackId = 3L,
            scheduleId = schedule.id,
            status = StudyStatus.PENDING
        )
    }

    private fun createStudyWithName(schedule: StudySchedule, name: String): Study {
        return Study(
            budget = BudgetType.BOOK,
            name = name,
            description = "스터디 소개글",
            leaderId = 1L,
            trackId = 3L,
            scheduleId = schedule.id,
            status = StudyStatus.PENDING
        )
    }

    private fun createStudyWithDescription(schedule: StudySchedule, description: String): Study {
        return Study(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = description,
            leaderId = 1L,
            trackId = 3L,
            scheduleId = schedule.id,
            status = StudyStatus.PENDING
        )
    }

    private fun createStudyWithExternalChatUrl(schedule: StudySchedule, externalChatUrl: String): Study {
        return Study(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = "유효한 소개글",
            leaderId = 1L,
            trackId = 3L,
            scheduleId = schedule.id,
            status = StudyStatus.PENDING,
            externalChatUrl = externalChatUrl
        )
    }

    private fun createStudyWithReferenceUrl(schedule: StudySchedule, referenceUrl: String): Study {
        return Study(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = "유효한 소개글",
            leaderId = 1L,
            trackId = 3L,
            scheduleId = schedule.id,
            status = StudyStatus.PENDING,
            referenceUrl = referenceUrl
        )
    }

    // ----- helpers

    private fun updateStudyCapacity(study: Study, capacity: Int, isRecruitmentClosed: Boolean) {
        return study.updateStudyInfo(
            newCapacity = capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newTags = NOT_GONNA_CHANGE,
            newScheduleId = study.scheduleId,
            isRecruitmentClosed = isRecruitmentClosed
        )
    }

    private fun updateStudyName(study: Study, name: String, isRecruitmentClosed: Boolean) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = name,
            newDescription = study.description,
            newBudget = study.budget,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newTags = NOT_GONNA_CHANGE,
            newScheduleId = study.scheduleId,
            isRecruitmentClosed = isRecruitmentClosed
        )
    }

    private fun updateStudyDescription(study: Study, description: String, isRecruitmentClosed: Boolean) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = description,
            newBudget = study.budget,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newTags = NOT_GONNA_CHANGE,
            newScheduleId = study.scheduleId,
            isRecruitmentClosed = isRecruitmentClosed
        )
    }

    private fun updateStudyExternalChatUrl(study: Study, externalChatUrl: String, isRecruitmentClosed: Boolean) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newChatUrl = externalChatUrl,
            newRefUrl = study.referenceUrl,
            newTags = NOT_GONNA_CHANGE,
            newScheduleId = study.scheduleId,
            isRecruitmentClosed = isRecruitmentClosed
        )
    }

    private fun updateStudyReferenceUrl(study: Study, referenceUrl: String, isRecruitmentClosed: Boolean) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newChatUrl = study.externalChatUrl,
            newRefUrl = referenceUrl,
            newTags = NOT_GONNA_CHANGE,
            newScheduleId = study.scheduleId,
            isRecruitmentClosed = isRecruitmentClosed
        )
    }
}