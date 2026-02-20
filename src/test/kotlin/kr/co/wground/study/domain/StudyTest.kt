package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study_schedule.domain.enums.Months
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import kr.co.wground.study_schedule.domain.StudySchedule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

@DisplayName("스터디(Study) 테스트")
class StudyTest {

    // To Do: 특정 값을 변경하고 싶지 않은 경우, null 을 명시적으로 전달하는 방식이 괜찮을 지 모르겠음
    val NOT_GONNA_CHANGE = null
    val THE_LEADER_ID = 1L
    val THE_OTHER_USER_ID = 2L

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

        @JvmStatic
        fun invalidBudgetExplains(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", ""),
            Arguments.of("blank(space)", " "),
            Arguments.of("blank(tab)", "\t"),
            Arguments.of("blank(newline)", "\n"),
            Arguments.of("blank(mixed)", " \t \n "),
            Arguments.of("too short(${Study.MIN_BUDGET_EXPLAIN_LENGTH}자 미만)", "1".repeat(Study.MIN_BUDGET_EXPLAIN_LENGTH - 1)),
            Arguments.of("too short(${Study.MIN_BUDGET_EXPLAIN_LENGTH}자 미만; trimmed)", " 1 "),
            Arguments.of("too short(${Study.MIN_BUDGET_EXPLAIN_LENGTH}자 미만; mixed)", " \t1\n "),
            Arguments.of("too long(${Study.MAX_BUDGET_EXPLAIN_LENGTH}자 초과)", " \t" + "1".repeat(Study.MAX_BUDGET_EXPLAIN_LENGTH + 1) + "\n ")
        )

        @JvmStatic
        fun invalidWeeklyPlanInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", ""),
            Arguments.of("blank(space)", " "),
            Arguments.of("blank(tab)", "\t"),
            Arguments.of("blank(newline)", "\n"),
            Arguments.of("blank(mixed)", " \t \n "),
            Arguments.of("too short(${WeeklyPlans.MIN_PLAN_LENGTH}자 미만)", "1".repeat(WeeklyPlans.MIN_PLAN_LENGTH - 1)),
            Arguments.of("too short(${WeeklyPlans.MIN_PLAN_LENGTH}자 미만; trimmed)", " 1 "),
            Arguments.of("too short(${WeeklyPlans.MIN_PLAN_LENGTH}자 미만; mixed)", " \t1\n "),
            Arguments.of("too long(${WeeklyPlans.MAX_PLAN_LENGTH}자 초과)", " \t" + "1".repeat(WeeklyPlans.MAX_PLAN_LENGTH + 1) + "\n ")
        )

        @JvmStatic
        fun invalidWeeklyPlansForCreateOrUpdate(): Stream<Arguments> {
            val validWeek1Plan = "1주차 계획"
            val validWeek2Plan = "2주차 계획"
            val validWeek3Plan = "3주차 계획"
            val validWeek4Plan = "4주차 계획"

            return invalidWeeklyPlanInputs().flatMap { argument ->
                val caseName = argument.get()[0] as String
                val invalidPlan = argument.get()[1] as String

                Stream.of(
                    Arguments.of("1주차-$caseName", invalidPlan, validWeek2Plan, validWeek3Plan, validWeek4Plan),
                    Arguments.of("2주차-$caseName", validWeek1Plan, invalidPlan, validWeek3Plan, validWeek4Plan),
                    Arguments.of("3주차-$caseName", validWeek1Plan, validWeek2Plan, invalidPlan, validWeek4Plan),
                    Arguments.of("4주차-$caseName", validWeek1Plan, validWeek2Plan, validWeek3Plan, invalidPlan),
                )
            }
        }
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
            updateStudyCapacity(created, givenCapacity)
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

            updateStudyName(created, givenTitle)
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

            updateStudyDescription(created, givenDescription)
        }

        assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
    }

    // ----- 지원 항목 설명

    @ParameterizedTest(name = "희망 지원 항목 설명: {0}")
    @MethodSource("invalidBudgetExplains")
    @DisplayName("스터디 생성 시, 앞뒤 공백 제거 기준, 희망 지원 항목 설명이 유효하지 않은 경우, 예외 발생 - BusinessException(STUDY_BUDGET_EXPLAIN_INVALID)")
    fun shouldThrowStudyBudgetExplainInvalid_whenCreateStudyWithInvalidBudgetExplain(caseName: String, givenBudgetExplain: String) {
        val thrown = assertThrows<BusinessException> {
            createStudyWithBudgetExplain(createRecruitingStudySchedule(), givenBudgetExplain)
        }

        assertEquals(StudyDomainErrorCode.STUDY_BUDGET_EXPLAIN_INVALID.code, thrown.code)
    }

    @ParameterizedTest(name = "희망 지원 항목 설명: {0}")
    @MethodSource("invalidBudgetExplains")
    @DisplayName("스터디 수정 시, 앞뒤 공백 제거 기준, 희망 지원 항목 설명이 유효하지 않은 경우, 예외 발생 - BusinessException(STUDY_BUDGET_EXPLAIN_INVALID)")
    fun shouldThrowStudyBudgetExplainInvalid_whenUpdateStudyWithInvalidBudgetExplain(caseName: String, givenBudgetExplain: String) {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithBudgetExplain(schedule, "피자먹을래요")

            updateStudyBudgetExplain(created, givenBudgetExplain)
        }

        assertEquals(StudyDomainErrorCode.STUDY_BUDGET_EXPLAIN_INVALID.code, thrown.code)
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

            updateStudyExternalChatUrl(created, "유효하지 않은 형식의 링크")
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

            updateStudyReferenceUrl(created, "유효하지 않은 형식의 링크")
        }

        assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
    }

    // ----- 스터디 진행 계획

    @ParameterizedTest(name = "스터디 진행 계획: {0}")
    @MethodSource("invalidWeeklyPlansForCreateOrUpdate")
    @DisplayName("스터디 생성 시, 주차 별 진행 계획 중 하나라도 유효하지 않은 경우, 예외 발생 - BusinessException(WEEKLY_STUDY_PLANS_INVALID)")
    fun shouldThrowWeeklyStudyPlansInvalid_whenCreateStudyWithInvalidWeeklyPlans(
        caseName: String,
        week1Plan: String,
        week2Plan: String,
        week3Plan: String,
        week4Plan: String,
    ) {
        val thrown = assertThrows<BusinessException> {
            createStudyWithWeeklyPlans(
                schedule = createRecruitingStudySchedule(),
                week1Plan = week1Plan,
                week2Plan = week2Plan,
                week3Plan = week3Plan,
                week4Plan = week4Plan,
            )
        }

        assertEquals(StudyDomainErrorCode.WEEKLY_STUDY_PLANS_INVALID.code, thrown.code)
    }

    @ParameterizedTest(name = "스터디 진행 계획: {0}")
    @MethodSource("invalidWeeklyPlansForCreateOrUpdate")
    @DisplayName("스터디 수정 시, 주차 별 진행 계획 중 하나라도 유효하지 않은 경우, 예외 발생 - BusinessException(WEEKLY_STUDY_PLANS_INVALID)")
    fun shouldThrowWeeklyStudyPlansInvalid_whenUpdateStudyWithInvalidWeeklyPlans(
        caseName: String,
        week1Plan: String,
        week2Plan: String,
        week3Plan: String,
        week4Plan: String,
    ) {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyWithWeeklyPlans(
                schedule = createRecruitingStudySchedule(),
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            )

            updateStudyWeeklyPlans(
                study = created,
                week1Plan = week1Plan,
                week2Plan = week2Plan,
                week3Plan = week3Plan,
                week4Plan = week4Plan,
            )
        }

        assertEquals(StudyDomainErrorCode.WEEKLY_STUDY_PLANS_INVALID.code, thrown.code)
    }



    // ----- 진행 시작 테스트

    @Test
    @DisplayName("대상 스터디가 RECRUITING 상태일 때, 스터디 진행 시작 시, 예외 발생 - BusinessException(STUDY_MUST_BE_RECRUITING_CLOSED_TO_START)")
    fun shouldThrowStudyMustBeRecruitingClosedToStart_whenStartFromRecruiting() {

        val thrown = assertThrows<BusinessException> {
            val study = createStudyWithCapacity(createRecruitingStudySchedule(),2)
            study.start()
        }

        assertEquals(StudyDomainErrorCode.STUDY_MUST_BE_RECRUITING_CLOSED_TO_START.code, thrown.code)
    }

    @Test
    @DisplayName("스터디가 RECRUITING_CLOSED 상태이고 최소 인원에 미달되었을 때, 진행 시작 시도한 경우, 예외 발생 - BusinessException(STUDY_CANNOT_START_DUE_TO_NOT_ENOUGH_MEMBER)")
    fun shouldThrowStudyCannotStartDueToNotEnoughMember_whenStartWithoutMinimumMember() {

        val thrown = assertThrows<BusinessException> {
            val study = createStudyWithCapacity(createRecruitingStudySchedule(), Study.MIN_CAPACITY)
            study.closeRecruitment()
            study.start()
        }

        assertEquals(StudyDomainErrorCode.STUDY_CANNOT_START_DUE_TO_NOT_ENOUGH_MEMBER.code, thrown.code)
    }

    // ----- 참여 테스트

    @Test
    @DisplayName("IN_PROGRESS 상태에서, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(STUDY_NOT_RECRUITING)")
    fun shouldThrowStudyNotRecruiting_whenIncreaseMemberOnInProgressStudy() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, 3)
            created.participate(THE_OTHER_USER_ID)
            created.closeRecruitment()
            created.start()

            created.participate(THE_OTHER_USER_ID + 1)
        }

        assertEquals(StudyDomainErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
    }

    @Test
    @DisplayName("이미 정원이 가득 찬 경우, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(STUDY_CAPACITY_FULL)")
    fun shouldThrowStudyCapacityFull_whenIncreaseMemberAtCapacity() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)
            for (i in 0..<Study.MIN_CAPACITY) {
                created.participate(THE_OTHER_USER_ID + i)
            }
        }

        assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_FULL.code, thrown.code)
    }

    // ----- 수정 테스트

    @Test
    @DisplayName("IN_PROGRESS 상태인 경우, 스터디를 수정 시, 예외 발생 - BusinessException(STUDY_CANNOT_MODIFY_IN_PROGRESS_OR_COMPLETED)")
    fun shouldThrowStudyCannotModifyInProgressOrCompleted_whenUpdateInProgressStudy() {
        val thrown = assertThrows<BusinessException> {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)
            created.participate(THE_OTHER_USER_ID)
            created.closeRecruitment()
            created.start()

            updateStudyName(created, "제목제목")
        }

        assertEquals(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_IN_PROGRESS_OR_COMPLETED.code, thrown.code)
    }

    @Test
    @Disabled
    @DisplayName("RECRUITING 상태일 때, 생성 시점에 설정 가능한 임의의 항목을 수정할 시, 성공한다")
    fun shouldUpdateEditableFields_whenStudyRecruiting() {
        val schedule = createRecruitingStudySchedule()
        val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)

        val newCapacity = created.capacity + 1
        val newName = created.name + "a"
        val newDescription = created.description + "a"
        val newBudget = if (created.budget == BudgetType.BOOK) BudgetType.MEAL else BudgetType.BOOK
        val newBudgetExplain = if (newBudget == BudgetType.BOOK) "오브젝트" else "피자"
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
            newBudgetExplain = newBudgetExplain,
            newChatUrl = newChatUrl,
            newRefUrl = newRefUrl,
            newWeeklyPlans = created.weeklyPlans,
            newTags = newTags
        )

        // 문제 사유 addTag 내부에서 Tag 존재 여부를 id 로 구분하고 있으나, 새로 생성된 태그들은 모두 id 가 0 이어서 구분되지 않음.
        created.addTag(Tag.create("아마도새로운태그일걸요2"))

        assertAll(
            { assertEquals(newCapacity, created.capacity) },
            { assertEquals(newName, created.name) },
            { assertEquals(newDescription, created.description) },
            { assertEquals(newDescription, created.budgetExplain) },
            { assertEquals(newBudget, created.budget) },
            { assertEquals(newChatUrl, created.externalChatUrl) },
            { assertEquals(newRefUrl, created.referenceUrl) },
            { assertTrue { 1 == created.studyTags.filter { studyTag -> studyTag.tag.name.contains("아마도새로운태그일걸요1") }.size } },
            { assertTrue { 1 == created.studyTags.filter { studyTag -> studyTag.tag.name.contains("아마도새로운태그일걸요2") }.size } }
        )
    }

    // ----- factories

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
        return Study.createNew(
            capacity = capacity,
            budget = BudgetType.BOOK,
            name = "스터디 제목",
            description = "스터디 소개글",
            budgetExplain = "피자먹을래요",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
        )
    }

    private fun createStudyWithName(schedule: StudySchedule, name: String): Study {
        return Study.createNew(
            budget = BudgetType.BOOK,
            name = name,
            description = "스터디 소개글",
            budgetExplain = "피자먹을래요",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
        )
    }

    private fun createStudyWithDescription(schedule: StudySchedule, description: String): Study {
        return Study.createNew(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = description,
            budgetExplain = "피자먹을래요",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
        )
    }

    private fun createStudyWithBudgetExplain(schedule: StudySchedule, budgetExplain: String): Study {
        return Study.createNew(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = "스터디 소개글",
            budgetExplain = budgetExplain,
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
        )
    }

    private fun createStudyWithExternalChatUrl(schedule: StudySchedule, externalChatUrl: String): Study {
        return Study.createNew(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = "유효한 소개글",
            budgetExplain = "피자먹을래요",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
            externalChatUrl = externalChatUrl
        )
    }

    private fun createStudyWithReferenceUrl(schedule: StudySchedule, referenceUrl: String): Study {
        return Study.createNew(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = "유효한 소개글",
            budgetExplain = "피자먹을래요",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = "1주차 계획",
                week2Plan = "2주차 계획",
                week3Plan = "3주차 계획",
                week4Plan = "4주차 계획",
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
            referenceUrl = referenceUrl
        )
    }

    private fun createStudyWithWeeklyPlans(
        schedule: StudySchedule,
        week1Plan: String,
        week2Plan: String,
        week3Plan: String,
        week4Plan: String,
    ): Study {
        return Study.createNew(
            budget = BudgetType.BOOK,
            name = "유효한 제목",
            description = "유효한 소개글",
            budgetExplain = "피자먹을래요",
            weeklyPlans = WeeklyPlans.of(
                week1Plan = week1Plan,
                week2Plan = week2Plan,
                week3Plan = week3Plan,
                week4Plan = week4Plan,
            ),
            leaderId = THE_LEADER_ID,
            trackId = 3L,
            scheduleId = schedule.id,
        )
    }

    // ----- helpers

    private fun updateStudyCapacity(study: Study, capacity: Int) {
        return study.updateStudyInfo(
            newCapacity = capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newBudgetExplain = study.budgetExplain,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newWeeklyPlans = study.weeklyPlans,
            newTags = NOT_GONNA_CHANGE
        )
    }

    private fun updateStudyName(study: Study, name: String) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = name,
            newDescription = study.description,
            newBudget = study.budget,
            newBudgetExplain = study.budgetExplain,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newWeeklyPlans = study.weeklyPlans,
            newTags = NOT_GONNA_CHANGE
        )
    }

    private fun updateStudyDescription(study: Study, description: String) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = description,
            newBudget = study.budget,
            newBudgetExplain = study.budgetExplain,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newWeeklyPlans = study.weeklyPlans,
            newTags = NOT_GONNA_CHANGE
        )
    }

    private fun updateStudyBudgetExplain(study: Study, budgetExplain: String) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newBudgetExplain = budgetExplain,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newWeeklyPlans = study.weeklyPlans,
            newTags = NOT_GONNA_CHANGE
        )
    }

    private fun updateStudyExternalChatUrl(study: Study, externalChatUrl: String) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newBudgetExplain = study.budgetExplain,
            newChatUrl = externalChatUrl,
            newRefUrl = study.referenceUrl,
            newWeeklyPlans = study.weeklyPlans,
            newTags = NOT_GONNA_CHANGE
        )
    }

    private fun updateStudyReferenceUrl(study: Study, referenceUrl: String) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newBudgetExplain = study.budgetExplain,
            newChatUrl = study.externalChatUrl,
            newRefUrl = referenceUrl,
            newWeeklyPlans = study.weeklyPlans,
            newTags = NOT_GONNA_CHANGE
        )
    }

    private fun updateStudyWeeklyPlans(
        study: Study,
        week1Plan: String,
        week2Plan: String,
        week3Plan: String,
        week4Plan: String,
    ) {
        return study.updateStudyInfo(
            newCapacity = study.capacity,
            newName = study.name,
            newDescription = study.description,
            newBudget = study.budget,
            newBudgetExplain = study.budgetExplain,
            newChatUrl = study.externalChatUrl,
            newRefUrl = study.referenceUrl,
            newWeeklyPlans = WeeklyPlans.of(
                week1Plan = week1Plan,
                week2Plan = week2Plan,
                week3Plan = week3Plan,
                week4Plan = week4Plan,
            ),
            newTags = NOT_GONNA_CHANGE
        )
    }
}
