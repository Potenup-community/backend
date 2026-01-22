package kr.co.wground.study.presentation.request.study

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyUpdateCommand
import kr.co.wground.study.domain.constant.BudgetType

data class StudyUpdateRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:NotBlank
    @field:Size(max = 1000)
    val description: String,

    @field:Min(2)
    val capacity: Int,

    val budget: BudgetType,

    @field:NotBlank
    val chatUrl: String,

    val refUrl: String?,

    @field:Size(max = 5)
    val tags: List<String> = emptyList()
) {
    fun toCommand(studyId: Long, userId: UserId): StudyUpdateCommand {
        return StudyUpdateCommand(
            studyId = studyId,
            userId = userId,
            name = name,
            description = description,
            capacity = capacity,
            budget = budget,
            chatUrl = chatUrl,
            refUrl = refUrl,
            tags = tags
        )
    }
}
