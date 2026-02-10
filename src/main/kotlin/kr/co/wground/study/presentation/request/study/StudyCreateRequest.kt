package kr.co.wground.study.presentation.request.study

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.domain.enums.BudgetType

data class StudyCreateRequest(
    @field:NotBlank(message = "이름은 빈 값일수 없습니다.")
    @field:Size(min = 2, max = 50)
    val name: String,

    @field:NotBlank(message = "스터디 설명은 필수 값입니다.")
    @field:Size(max = 300, message = "스터디 설명은 최대 300자까지 작성할 수 있습니다.")
    val description: String,

    @field:Min(2, message = "모집 최소 정원은 2명입니다.")
    val capacity: Int,

    @field:NotNull(message = "지원금 유형은 필수 값입니다.")
    val budget: BudgetType,

    @field:NotBlank(message = "희망 지원 항목 설명은 비어있을 수 없습니다.")
    val budgetExplain: String,

    val chatUrl: String,

    val refUrl: String?,

    @field:Size(max = 5, message = "태그는 최대 5개까지 가질 수 있습니다.")
    val tags: List<String> = emptyList()
) {
    fun toCommand(userId: UserId): StudyCreateCommand {
        return StudyCreateCommand(
            userId = userId,
            name = name,
            description = description,
            capacity = capacity,
            budget = budget,
            budgetExplain = budgetExplain,
            chatUrl = chatUrl,
            refUrl = refUrl,
            tags = tags
        )
    }
}