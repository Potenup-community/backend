package kr.co.wground.study.presentation.response.tag

import kr.co.wground.study.application.dto.TagResult

data class TagResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(tag: TagResult): TagResponse {
            return TagResponse(
                id = tag.id,
                name = tag.name
            )
        }
    }
}