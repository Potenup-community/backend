package kr.co.wground.shop.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ShopErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SH-0001", "아이템을 찾을 수 없습니다."),
    ITEM_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "SH-0002", "현재 판매 중이 아닌 아이템입니다."),
    ALREADY_OWNED(HttpStatus.CONFLICT, "SH-0003", "이미 보유 중인 영구 아이템입니다."),
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "SH-0004", "인벤토리 항목을 찾을 수 없습니다."),
    ITEM_EXPIRED(HttpStatus.BAD_REQUEST, "SH-0005", "만료된 아이템입니다."),
    NOT_OWNER(HttpStatus.FORBIDDEN, "SH-0006", "해당 아이템의 소유자가 아닙니다."),
    INVALID_PRICE_TO_CREATE(HttpStatus.BAD_REQUEST,"SH-0007","가격은 0보다 커야 합니다."),
    CONSUMABLE_ITEM_NEED_DURATION(HttpStatus.BAD_REQUEST,"SH-0008","기간제 아이템은 유효 일수가 필요합니다."),
    ALREADY_PERMANENT(HttpStatus.BAD_REQUEST,"SH-0009","이미 구매한 영구 아이템입니다."),
    DURATION_DAYS_MUST_POSITIVE(HttpStatus.BAD_REQUEST,"SH-0010","아이템 기간은 0보다 커야 합니다."),
    PERMANENT_ITEM_SHOULD_NOT_HAVE_DURATION(HttpStatus.BAD_REQUEST,"SH-0011", "영구 아이템은 기간을 가질 수 없습니다."),
    INVALID_ITEM_NAME(HttpStatus.BAD_REQUEST,"SH-0012","아이템 이름은 빈 값일수 없습니다."),
    INVALID_ITEM_DESCRIPTION(HttpStatus.BAD_REQUEST,"SH-0013","아이템 설명은 빈 값일수 없습니다."),
    INVALID_ITEM_IMAGE_URL(HttpStatus.BAD_REQUEST,"SH-0014","아이템 이미지는 빈 값일수 없습니다."),

}