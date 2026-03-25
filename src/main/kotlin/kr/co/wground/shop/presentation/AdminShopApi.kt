package kr.co.wground.shop.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.shop.docs.ShopSwaggerErrorExample
import kr.co.wground.shop.docs.ShopSwaggerResponseExample
import kr.co.wground.shop.presentation.request.AdminShopItemCreateRequest
import kr.co.wground.shop.presentation.request.AdminShopItemUpdateRequest
import kr.co.wground.shop.presentation.response.ShopItemDetailResponse
import org.springframework.http.ResponseEntity

@Tag(name = "Admin - Shop", description = "관리자 상점 아이템 API")
interface AdminShopApi {

    @Operation(
        summary = "상점 아이템 등록",
        description = """새로운 상점 아이템을 등록합니다. multipart/form-data로 전송합니다.

  - file: png, svg, gif, webp만 허용 (최대 5MB)
  - consumable=true(기간제): durationDays 필수 (1 이상)
  - consumable=false(영구제): durationDays는 null이어야 합니다.
  - itemType: BADGE, PET, FRAME"""
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "등록 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ShopItemDetailResponse::class),
                    examples = [
                        ExampleObject(name = "PERMANENT_ITEM", value = ShopSwaggerResponseExample.CREATED_ITEM),
                        ExampleObject(name = "CONSUMABLE_ITEM", value = ShopSwaggerResponseExample.CREATED_CONSUMABLE_ITEM),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "FILE_EMPTY", value = ShopSwaggerErrorExample.BadRequest.FILE_EMPTY),
                        ExampleObject(name = "FILE_TOO_LARGE", value = ShopSwaggerErrorExample.BadRequest.FILE_TOO_LARGE),
                        ExampleObject(name = "UNSUPPORTED_EXTENSION", value = ShopSwaggerErrorExample.BadRequest.UNSUPPORTED_EXTENSION),
                        ExampleObject(name = "UNSUPPORTED_FORMAT", value = ShopSwaggerErrorExample.BadRequest.UNSUPPORTED_FORMAT),
                        ExampleObject(name = "UPLOAD_IO_ERROR", value = ShopSwaggerErrorExample.BadRequest.UPLOAD_IO_ERROR),
                        ExampleObject(name = "INVALID_ITEM_NAME", value = ShopSwaggerErrorExample.BadRequest.INVALID_ITEM_NAME),
                        ExampleObject(name = "INVALID_ITEM_DESCRIPTION", value = ShopSwaggerErrorExample.BadRequest.INVALID_ITEM_DESCRIPTION),
                        ExampleObject(name = "INVALID_PRICE", value = ShopSwaggerErrorExample.BadRequest.INVALID_PRICE),
                        ExampleObject(name = "CONSUMABLE_NEED_DURATION", value = ShopSwaggerErrorExample.BadRequest.CONSUMABLE_NEED_DURATION),
                        ExampleObject(name = "PERMANENT_NO_DURATION", value = ShopSwaggerErrorExample.BadRequest.PERMANENT_NO_DURATION),
                        ExampleObject(name = "INVALID_INPUT", value = ShopSwaggerErrorExample.BadRequest.INVALID_INPUT),
                    ]
                )]
            ),
        ]
    )
    fun createItem(request: AdminShopItemCreateRequest): ResponseEntity<ShopItemDetailResponse>

    @Operation(
        summary = "상점 아이템 수정",
        description = """기존 상점 아이템의 이름, 설명, 가격을 수정합니다. multipart/form-data로 전송합니다.

  - file: 이미지를 변경할 경우에만 첨부 (미첨부 시 기존 이미지 유지)
  - itemType, consumable, durationDays는 구조적 속성으로 변경 불가합니다."""
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ShopItemDetailResponse::class),
                    examples = [
                        ExampleObject(name = "UPDATED_ITEM", value = ShopSwaggerResponseExample.UPDATED_ITEM),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "FILE_TOO_LARGE", value = ShopSwaggerErrorExample.BadRequest.FILE_TOO_LARGE),
                        ExampleObject(name = "UNSUPPORTED_EXTENSION", value = ShopSwaggerErrorExample.BadRequest.UNSUPPORTED_EXTENSION),
                        ExampleObject(name = "UNSUPPORTED_FORMAT", value = ShopSwaggerErrorExample.BadRequest.UNSUPPORTED_FORMAT),
                        ExampleObject(name = "INVALID_ITEM_NAME", value = ShopSwaggerErrorExample.BadRequest.INVALID_ITEM_NAME),
                        ExampleObject(name = "INVALID_ITEM_DESCRIPTION", value = ShopSwaggerErrorExample.BadRequest.INVALID_ITEM_DESCRIPTION),
                        ExampleObject(name = "INVALID_PRICE", value = ShopSwaggerErrorExample.BadRequest.INVALID_PRICE),
                        ExampleObject(name = "INVALID_INPUT", value = ShopSwaggerErrorExample.BadRequest.INVALID_INPUT),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "아이템을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "ITEM_NOT_FOUND", value = ShopSwaggerErrorExample.NotFound.ITEM_NOT_FOUND),
                    ]
                )]
            ),
        ]
    )
    fun updateItem(
        @Parameter(description = "수정할 아이템 ID", example = "1") itemId: Long,
        request: AdminShopItemUpdateRequest,
    ): ResponseEntity<ShopItemDetailResponse>

    @Operation(
        summary = "상점 아이템 숨김 처리",
        description = """아이템을 HIDDEN 상태로 변경합니다.

  - 이미지 파일은 삭제되지 않습니다 (기존 구매자 보호, 재활성화 가능).
  - 숨김 처리된 아이템은 상점 목록에서 제외되며 신규 구매가 불가합니다."""
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "숨김 처리 성공"),
            ApiResponse(
                responseCode = "404",
                description = "아이템을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "ITEM_NOT_FOUND", value = ShopSwaggerErrorExample.NotFound.ITEM_NOT_FOUND),
                    ]
                )]
            ),
        ]
    )
    fun hideItem(
        @Parameter(description = "숨김 처리할 아이템 ID", example = "1") itemId: Long,
    ): ResponseEntity<Unit>
}