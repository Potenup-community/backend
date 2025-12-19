package kr.co.wground.track.presentation

import jakarta.validation.Valid
import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.TrackService
import kr.co.wground.track.presentation.request.CreateTrackRequest
import kr.co.wground.track.presentation.request.UpdateTrackRequest
import kr.co.wground.track.presentation.request.toCreateTrackDto
import kr.co.wground.track.presentation.request.toUpdateTrackDto
import kr.co.wground.track.presentation.response.TrackQueryResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/tracks")
class TrackController(
    private val trackService: TrackService,
) {
    @PostMapping
    fun createTrack(@RequestBody @Valid createTrack: CreateTrackRequest): ResponseEntity<List<TrackQueryResponse>> {
        val response = trackService.createTrack(createTrack.toCreateTrackDto())
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/{id}")
    fun updateTrack(
        @PathVariable("id") trackId: TrackId,
        @RequestBody @Valid updateTrack: UpdateTrackRequest
    ): ResponseEntity<Unit> {
        trackService.updateTrack(updateTrack.toUpdateTrackDto(trackId))
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun deleteTrack(
        @PathVariable("id") trackId: TrackId,
    ): ResponseEntity<Unit> {
        trackService.deleteTrack(trackId)
        return ResponseEntity.noContent().build()
    }
}
