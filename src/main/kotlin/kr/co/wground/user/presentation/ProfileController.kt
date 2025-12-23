package kr.co.wground.user.presentation

import kr.co.wground.global.common.UserId
import kr.co.wground.user.utils.defaultimage.application.ProfileService
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/v1/profiles")
class ProfileController(
    private val profileService: ProfileService,
) {
    @GetMapping("/{userId}")
    fun getProfileImage(@PathVariable userId: UserId, request: WebRequest): ResponseEntity<Resource> {
        val imageResponse = profileService.getProfileImageResource(userId)

        if (request.checkNotModified(imageResponse.lastModifiedAt)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(imageResponse.contentType))
            .cacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS).mustRevalidate().cachePublic())
            .lastModified(imageResponse.lastModifiedAt)
            .body(imageResponse.resource)
    }
}
