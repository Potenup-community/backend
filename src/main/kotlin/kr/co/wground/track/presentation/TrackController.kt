package kr.co.wground.track.presentation

import jakarta.validation.Valid
import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.TrackService
import kr.co.wground.track.application.dto.TrackQueryDto
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.presentation.request.CreateTrackRequest
import kr.co.wground.track.presentation.request.UpdateTrackRequest
import kr.co.wground.track.presentation.request.toCreateTrackDto
import kr.co.wground.track.presentation.request.toUpdateTrackDto
import kr.co.wground.track.presentation.response.TrackCardinalsResponse
import kr.co.wground.track.presentation.response.TrackListResponse
import kr.co.wground.track.presentation.response.TrackTypesResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/tracks")
class TrackController(
    private val trackService: TrackService,
): TrackApi {
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    override fun createTrack(@RequestBody @Valid createTrack: CreateTrackRequest): ResponseEntity<TrackListResponse<TrackQueryDto>> {
        val response = trackService.createTrack(createTrack.toCreateTrackDto())
        return ResponseEntity.status(HttpStatus.CREATED).body(TrackListResponse(response))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    override fun updateTrack(
        @PathVariable("id") trackId: TrackId,
        @RequestBody @Valid updateTrack: UpdateTrackRequest
    ): ResponseEntity<Unit> {
        trackService.updateTrack(updateTrack.toUpdateTrackDto(trackId))
        return ResponseEntity.noContent().build()
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    override fun deleteTrack(
        @PathVariable("id") trackId: TrackId,
    ): ResponseEntity<Unit> {
        trackService.deleteTrack(trackId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    override fun getTracksExceptAdmin(
        @RequestParam(required = false) trackType: TrackType?
    ): ResponseEntity<TrackListResponse<TrackQueryDto>> {
        val responses = trackService.getTracksExceptAdmin(trackType)
        return ResponseEntity.ok(TrackListResponse(responses))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    override fun getAllTracks(
        @RequestParam(required = false) trackType: TrackType?
    ): ResponseEntity<TrackListResponse<TrackQueryDto>> {
        val responses = trackService.getAllTrackResponses(trackType)
            .filterNot { it.trackType == TrackType.ADMIN }
        return ResponseEntity.ok(TrackListResponse(responses))
    }

    @GetMapping("/types")
    override fun getTrackTypesForRegistration(): ResponseEntity<TrackTypesResponse> {
        val response = trackService.getTrackTypesForRegistration()
        return ResponseEntity.ok(TrackTypesResponse.from(response))
    }

    @GetMapping("/cardinals")
    override fun getTrackCardinalsByType(
        @RequestParam trackType: TrackType
    ): ResponseEntity<TrackCardinalsResponse> {
        val response = trackService.getTrackCardinals(trackType)
        return ResponseEntity.ok(TrackCardinalsResponse.from(response))
    }
}
