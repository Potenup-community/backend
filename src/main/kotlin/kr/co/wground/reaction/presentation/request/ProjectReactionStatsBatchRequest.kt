package kr.co.wground.reaction.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.ProjectId

@Schema(description = "여러 프로젝트에 대한 리액션 정보 요청 바디")
data class ProjectReactionStatsBatchRequest(
    @field:Schema(example = """{"projectIds": [1, 2, 3]}""")
    @field:NotNull(message = "projectId 집합이 비어있습니다.")
    @field:Size(min = 1, max = 50, message = "projectId 집합이 너무 작거나 큽니다. 1 이상 50 이하여야 합니다.")
    val projectIds: Set<ProjectId>,
)
