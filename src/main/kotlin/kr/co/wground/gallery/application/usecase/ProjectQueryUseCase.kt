package kr.co.wground.gallery.application.usecase

import kr.co.wground.gallery.application.usecase.query.GetProjectDetailQuery
import kr.co.wground.gallery.application.usecase.query.GetProjectListQuery
import kr.co.wground.gallery.application.usecase.result.ProjectDetailResult
import kr.co.wground.gallery.application.usecase.result.ProjectSummaryResult
import kr.co.wground.gallery.application.usecase.result.TrackFilterResult
import org.springframework.data.domain.Page

interface ProjectQueryUseCase {
    fun getList(query: GetProjectListQuery): Page<ProjectSummaryResult>
    fun getDetail(query: GetProjectDetailQuery): ProjectDetailResult
    fun getTrackFilters(): List<TrackFilterResult>
}
