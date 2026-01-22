package kr.co.wground.study.presentation.request.study

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.domain.constant.BudgetType

data class StudyCreateRequest(
    @field:NotBlank
    @field:Size(min = 2, max =50)
    val name: String,

    @field:NotBlank
    @field:Size(max = 300)
    val description: String,

    @field:Min(2)
    val capacity: Int,

    @field:NotNull
    val budget: BudgetType,

    @field:NotBlank
    val chatUrl: String,

    val refUrl: String?,

    @field:Size(max = 5)
    val tags: List<String> = emptyList()
) {
    fun toCommand(userId: UserId): StudyCreateCommand {
        return StudyCreateCommand(
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