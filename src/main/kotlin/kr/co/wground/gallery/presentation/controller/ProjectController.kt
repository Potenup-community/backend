package kr.co.wground.gallery.presentation.controller

import jakarta.validation.Valid
import java.net.URI
import kr.co.wground.gallery.application.usecase.ProjectCommandUseCase
import kr.co.wground.gallery.presentation.ProjectApi
import kr.co.wground.gallery.presentation.request.CreateProjectRequest
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(
    private val projectCommandUseCase: ProjectCommandUseCase,
) : ProjectApi {

    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun createProject(
        userId: CurrentUserId,
        @Valid @RequestPart("data") request: CreateProjectRequest,
        @RequestPart("thumbnailImage") thumbnailImage: MultipartFile,
    ): ResponseEntity<Unit> {
        val projectId = projectCommandUseCase.create(request.toCommand(userId.value, thumbnailImage))
        val location = "/api/v1/projects/${projectId}"
        return ResponseEntity.created(URI.create(location)).build()
    }
}
