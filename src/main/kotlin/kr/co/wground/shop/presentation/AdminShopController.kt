package kr.co.wground.shop.presentation

import jakarta.validation.Valid
import kr.co.wground.shop.application.command.usecase.AdminShopItemUseCase
import kr.co.wground.shop.presentation.request.AdminShopItemCreateRequest
import kr.co.wground.shop.presentation.request.AdminShopItemUpdateRequest
import kr.co.wground.shop.presentation.response.ShopItemDetailResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/shop")
class AdminShopController(
    private val adminShopItemUseCase: AdminShopItemUseCase,
) : AdminShopApi {

    @PostMapping("/items", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override fun createItem(
        @ModelAttribute @Valid request: AdminShopItemCreateRequest,
    ): ResponseEntity<ShopItemDetailResponse> {
        val result = adminShopItemUseCase.createItem(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(ShopItemDetailResponse.from(result))
    }

    @PutMapping("/items/{itemId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override fun updateItem(
        @PathVariable itemId: Long,
        @ModelAttribute request: AdminShopItemUpdateRequest,
    ): ResponseEntity<ShopItemDetailResponse> {

        val result = adminShopItemUseCase.updateItem(itemId, request.toCommand())
        return ResponseEntity.ok(ShopItemDetailResponse.from(result))
    }

    @PatchMapping("/items/{itemId}/hide")
    override fun hideItem(
        @PathVariable itemId: Long,
    ): ResponseEntity<Unit> {
        adminShopItemUseCase.hide(itemId)
        return ResponseEntity.noContent().build()
    }
}