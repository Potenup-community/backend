package kr.co.wground.track.presentation

import kr.co.wground.track.application.TrackService
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.presentation.response.SignupTrackResolveResponse
import kr.co.wground.track.presentation.response.SignupTrackTypesResponse
import kr.co.wground.track.presentation.response.TrackCardinalsResponse
import org.springframework.validation.annotation.Validated
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v1/tracks/signup")
class SignupTrackController(
    private val trackService: TrackService,
) : SignupTrackApi {

    @GetMapping("/types")
    override fun getSignupTrackTypes(): ResponseEntity<SignupTrackTypesResponse> {
        val response = trackService.getSignupTrackTypes()
        return ResponseEntity.ok(SignupTrackTypesResponse.from(response))
    }

    @GetMapping("/cardinals")
    override fun getSignupCardinalsByType(
        @RequestParam trackType: TrackType
    ): ResponseEntity<TrackCardinalsResponse> {
        val response = trackService.getTrackCardinals(trackType)
        return ResponseEntity.ok(TrackCardinalsResponse.from(response))
    }

    @GetMapping("/resolve")
    override fun resolveSignupTrack(
        @RequestParam trackType: TrackType,
        @RequestParam cardinal: Int
    ): ResponseEntity<SignupTrackResolveResponse> {
        val response = trackService.resolveSignupTrack(trackType, cardinal)
        return ResponseEntity.ok(SignupTrackResolveResponse.from(response))
    }
}
