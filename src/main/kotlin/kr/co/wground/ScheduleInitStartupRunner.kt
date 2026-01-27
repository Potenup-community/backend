package kr.co.wground

import kr.co.wground.study.application.StudyScheduleService
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.domain.constant.Months
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ScheduleInitStartupRunner(
    private val scheduleService: StudyScheduleService
): CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        log.info("테스트 용 스터디 일정을 생성합니다. BE/FE 1기 5개월차 스터디가 존재하지 않으면 새로 생성합니다.")

        scheduleService

        val beTrackId = 2L
        val beScheduleCreateCommand = ScheduleCreateCommand(
            trackId = beTrackId,
            month = Months.FIFTH,
            recruitStartDate = LocalDate.of(2026, 1, 26),
            recruitEndDate = LocalDate.of(2026, 1, 29),
            studyEndDate = LocalDate.of(2026, 2, 27)
        )
        if (scheduleService.getCurrentSchedule(beTrackId) == null)  {
            scheduleService.createSchedule(beScheduleCreateCommand)
        }

        val feTrackId = 3L
        val feScheduleCreateCommand = ScheduleCreateCommand(
            trackId = feTrackId,
            month = Months.FIFTH,
            recruitStartDate = LocalDate.of(2026, 1, 26),
            recruitEndDate = LocalDate.of(2026, 1, 29),
            studyEndDate = LocalDate.of(2026, 2, 27)
        )
        if (scheduleService.getCurrentSchedule(feTrackId) == null)  {
            scheduleService.createSchedule(feScheduleCreateCommand)
        }
    }
}