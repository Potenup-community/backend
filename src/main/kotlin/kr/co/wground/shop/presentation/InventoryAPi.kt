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
import kr.co.wground.shop.presentation.response.InventoryItemGroupResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Inventory", description = "인벤토리 API")
interface InventoryApi {

    @Operation(summary = "내 인벤토리 조회", description = "보유 중인 아이템을 타입별로 그룹화하여 조회합니다. 만료된 아이템은 제외됩니다.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200", description = "조회 성공",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(name = "MY_INVENTORY", value = ShopSwaggerResponseExample.MY_INVENTORY)]
            )]
        )
    ])
    fun getMyInventory(
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string")) userId: CurrentUserId
    ): ResponseEntity<List<InventoryItemGroupResponse>>

    @Operation(summary = "아이템 장착", description = "인벤토리의 아이템을 장착합니다. 같은 타입의 기존 장착 아이템은 자동으로 해제됩니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "장착 성공"),
        ApiResponse(
            responseCode = "400", description = "장착 불가",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "ITEM_EXPIRED", value = ShopSwaggerErrorExample.BadRequest.ITEM_EXPIRED)]
            )]
        ),
        ApiResponse(
            responseCode = "403", description = "소유자 아님",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "NOT_OWNER", value = ShopSwaggerErrorExample.Forbidden.NOT_OWNER)]
            )]
        ),
        ApiResponse(
            responseCode = "404", description = "인벤토리 없음",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "INVENTORY_NOT_FOUND", value = ShopSwaggerErrorExample.NotFound.INVENTORY_NOT_FOUND)]
            )]
        )
    ])
    fun equipItem(
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string")) userId: CurrentUserId,
        @PathVariable inventoryId: Long,
    ): ResponseEntity<Unit>

    @Operation(summary = "아이템 해제", description = "장착 중인 아이템을 해제합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "해제 성공"),
        ApiResponse(
            responseCode = "403", description = "소유자 아님",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "NOT_OWNER", value = ShopSwaggerErrorExample.Forbidden.NOT_OWNER)]
            )]
        ),
        ApiResponse(
            responseCode = "404", description = "인벤토리 없음",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(name = "INVENTORY_NOT_FOUND", value = ShopSwaggerErrorExample.NotFound.INVENTORY_NOT_FOUND)]
            )]
        )
    ])
    fun unequipItem(
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string")) userId: CurrentUserId,
        @PathVariable inventoryId: Long,
    ): ResponseEntity<Unit>
}