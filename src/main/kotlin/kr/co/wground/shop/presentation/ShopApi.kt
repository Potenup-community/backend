package kr.co.wground.shop.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.shop.docs.ShopSwaggerErrorExample
import kr.co.wground.shop.docs.ShopSwaggerResponseExample
import kr.co.wground.shop.presentation.response.ShopItemDetailResponse
import kr.co.wground.shop.presentation.response.ShopItemGroupResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Shop", description = "상점 API")
interface ShopApi {

    @Operation(summary = "상점 아이템 목록 조회", description = "판매 중인 아이템을 타입별로 그룹화하여 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200", description = "조회 성공",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(name = "SHOP_ITEMS", value = ShopSwaggerResponseExample.SHOP_ITEMS)]
            )]
        )
    ])
    fun getShopItems(): ResponseEntity<List<ShopItemGroupResponse>>

    @Operation(summary = "상점 아이템 상세 조회", description = "아이템 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200", description = "조회 성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ShopItemDetailResponse::class),
                examples = [ExampleObject(name = "SHOP_ITEM_DETAIL", value = ShopSwaggerResponseExample.SHOP_ITEM_DETAIL)]
            )]
        ),
        ApiResponse(
            responseCode = "404", description = "아이템 없음",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "ITEM_NOT_FOUND", value = ShopSwaggerErrorExample.NotFound.ITEM_NOT_FOUND)]
            )]
        )
    ])
    fun getShopItemDetail(@PathVariable itemId: Long): ResponseEntity<ShopItemDetailResponse>

    @Operation(summary = "아이템 구매", description = "아이템을 구매합니다. 기간제 아이템을 이미 보유 중이면 영구권으로 업그레이드됩니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "구매 성공"),
        ApiResponse(
            responseCode = "400", description = "구매 불가",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [
                    ExampleObject(name = "ITEM_NOT_AVAILABLE", value = ShopSwaggerErrorExample.BadRequest.ITEM_NOT_AVAILABLE),
                    ExampleObject(name = "ALREADY_OWNED_ITEM", value = ShopSwaggerErrorExample.BadRequest.ALREADY_OWNED_ITEM),
                ]
            )]
        ),
        ApiResponse(
            responseCode = "404", description = "아이템 없음",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "ITEM_NOT_FOUND", value = ShopSwaggerErrorExample.NotFound.ITEM_NOT_FOUND)]
            )]
        ),
        ApiResponse(
            responseCode = "409", description = "이미 보유 중",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "ALREADY_OWNED", value = ShopSwaggerErrorExample.Conflict.ALREADY_OWNED)]
            )]
        )
    ])
    fun purchaseItem(
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string")) userId: CurrentUserId,
        @PathVariable itemId: Long,
    ): ResponseEntity<Unit>
}