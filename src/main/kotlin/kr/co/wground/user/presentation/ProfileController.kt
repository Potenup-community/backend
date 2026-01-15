package kr.co.wground.user.presentation

import kr.co.wground.global.common.UserId
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest

@RestController
@RequestMapping("/api/v1/users/profiles")
class ProfileController(
): ProfileApi {
    @GetMapping("/{userId}")
    override fun getProfileImage(@PathVariable userId: UserId, request: WebRequest): ResponseEntity<Resource> {
        TODO()
    }
}
