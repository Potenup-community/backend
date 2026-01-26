package kr.co.wground.user.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.user.application.operations.UserService
import kr.co.wground.user.presentation.response.ProfileResponse
import kr.co.wground.user.utils.defaultimage.application.ProfileImageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/users/profiles")
class ProfileController(
    private val profileImageService: ProfileImageService,
    private val userService: UserService,
) : ProfileApi {
    @GetMapping("/me")
    override fun getMyProfile(userId: CurrentUserId, request: WebRequest): ResponseEntity<ProfileResponse> {
        val user = userService.getMyInfo(userId.value)
        return ResponseEntity.ok().body(ProfileResponse.from(user))
    }

    @PostMapping("/me", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override fun uploadProfileImage(
        @RequestPart("file") file: MultipartFile,
        userId: CurrentUserId
    ): ResponseEntity<Unit> {
        profileImageService.updateProfileImage(userId.value, file)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/me")
    override fun deleteProfile(userId: CurrentUserId): ResponseEntity<Unit> {
        profileImageService.deleteProfileImage(userId.value)
        return ResponseEntity.noContent().build()
    }
}

