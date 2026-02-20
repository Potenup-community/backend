package kr.co.wground.gallery.presentation.controller

import jakarta.validation.Valid
import java.net.URI
import kr.co.wground.gallery.application.usecase.ProjectCommandUseCase
import kr.co.wground.gallery.application.usecase.ProjectQueryUseCase
import kr.co.wground.gallery.application.usecase.query.GetProjectDetailQuery
import kr.co.wground.gallery.application.usecase.query.GetProjectListQuery
import kr.co.wground.gallery.presentation.ProjectApi
import kr.co.wground.gallery.presentation.request.CreateProjectRequest
import kr.co.wground.gallery.presentation.response.ProjectDetailResponse
import kr.co.wground.gallery.presentation.response.ProjectSummaryPageResponse
import kr.co.wground.gallery.presentation.response.ProjectTrackFiltersResponse
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(
    private val projectCommandUseCase: ProjectCommandUseCase,
    private val projectQueryUseCase: ProjectQueryUseCase,
) : ProjectApi {

    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun createProject(
        userId: CurrentUserId,
        @Valid @RequestPart("data") request: CreateProjectRequest,
        @RequestPart("thumbnailImage") thumbnailImage: MultipartFile,
    ): ResponseEntity<Unit> {
        val projectId = projectCommandUseCase.create(request.toCommand(userId.value, thumbnailImage))
        val location = "/api/v1/projects/$projectId"
        return ResponseEntity.created(URI.create(location)).build()
    }

    @GetMapping
    override fun getProjects(
        @RequestParam(required = false) trackId: TrackId?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int,
        @RequestParam(defaultValue = "createdAt,desc") sort: String,
        user: CurrentUserId,
    ): ResponseEntity<ProjectSummaryPageResponse> {
        val result = projectQueryUseCase.getList(GetProjectListQuery(trackId, keyword, page, size, sort, user.value))
        return ResponseEntity.ok(ProjectSummaryPageResponse.from(result))
    }

    @GetMapping("/tracks")
    override fun getTrackFilters(): ResponseEntity<ProjectTrackFiltersResponse> {
        val result = projectQueryUseCase.getTrackFilters()
        return ResponseEntity.ok(ProjectTrackFiltersResponse.from(result))
    }

    @GetMapping("/{projectId}")
    override fun getProject(
        @PathVariable projectId: ProjectId,
        user: CurrentUserId,
    ): ResponseEntity<ProjectDetailResponse> {
        val result = projectQueryUseCase.getDetail(GetProjectDetailQuery(projectId, user.value))
        return ResponseEntity.ok(ProjectDetailResponse.from(result))
    }
}
