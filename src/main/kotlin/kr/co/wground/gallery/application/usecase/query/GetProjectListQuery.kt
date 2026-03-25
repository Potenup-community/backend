package kr.co.wground.gallery.application.usecase.query

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

data class GetProjectListQuery(
    val trackId: TrackId? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 12,
    val sort: String = "createdAt,desc",
    val userId: UserId,
) {
    fun toPageRequest(): PageRequest {
        val parts = sort.split(",")
        val property = parts.getOrElse(0) { "createdAt" }.trim()
        val direction = if (parts.getOrElse(1) { "desc" }.trim() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        return PageRequest.of(page, size, direction, property)
    }
}
