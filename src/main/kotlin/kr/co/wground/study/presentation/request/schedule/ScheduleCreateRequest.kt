package kr.co.wground.study.presentation.request.schedule

import jakarta.validation.constraints.NotNull
import kr.co.wground.global.common.TrackId
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.domain.constant.Months
import java.time.LocalDate

data class ScheduleCreateRequest(
    @field:NotNull(message = "트랙 아이디는 필수 입니다.")
    val trackId: TrackId,
    @field:NotNull(message = "차수는 필수 입니다.")
    val month: Months,
    @field:NotNull(message = "모집 시작일자은 필수 입니다.")
    val recruitStartDate: LocalDate,
    @field:NotNull(message = "모집 종료일자은 필수 입니다.")
    val recruitEndDate: LocalDate,
    @field:NotNull(message = "스터디 종료일자는 필수 입니다")
    val studyEndDate: LocalDate,
) {
    fun toCommand(): ScheduleCreateCommand {
        return ScheduleCreateCommand(
            trackId = this.trackId,
            month = this.month,
            recruitStartDate = this.recruitStartDate,
            recruitEndDate = this.recruitEndDate,
            studyEndDate = this.studyEndDate,
        )
    }
}