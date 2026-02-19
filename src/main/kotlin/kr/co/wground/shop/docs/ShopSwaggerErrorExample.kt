package kr.co.wground.shop.docs

object ShopSwaggerErrorExample {
    object NotFound {
        const val ITEM_NOT_FOUND = """{
              "code": "SH-0001",
              "message": "아이템을 찾을 수 없습니다.",
              "errors": []
          }"""
    }

    object BadRequest {
        const val INVALID_ITEM_NAME = """{
              "code": "SH-0012",
              "message": "아이템 이름은 빈 값일수 없습니다.",
              "errors": []
          }"""

        const val INVALID_ITEM_DESCRIPTION = """{
              "code": "SH-0013",
              "message": "아이템 설명은 빈 값일수 없습니다.",
              "errors": []
          }"""

        const val INVALID_PRICE = """{
              "code": "SH-0007",
              "message": "가격은 0보다 커야 합니다.",
              "errors": []
          }"""

        const val CONSUMABLE_NEED_DURATION = """{
              "code": "SH-0008",
              "message": "기간제 아이템은 유효 일수가 필요합니다.",
              "errors": []
          }"""

        const val PERMANENT_NO_DURATION = """{
              "code": "SH-0011",
              "message": "영구 아이템은 기간을 가질 수 없습니다.",
              "errors": []
          }"""

        const val FILE_EMPTY = """{
              "code": "UPL-0001",
              "message": "업로드할 파일이 비어 있습니다.",
              "errors": []
          }"""

        const val FILE_TOO_LARGE = """{
              "code": "UPL-0002",
              "message": "업로드할 파일이 너무 큽니다. (최대 5MB)",
              "errors": []
          }"""

        const val UNSUPPORTED_EXTENSION = """{
              "code": "UPL-0003",
              "message": "지원하지 않는 확장자입니다.",
              "errors": []
          }"""

        const val UNSUPPORTED_FORMAT = """{
              "code": "UPL-0004",
              "message": "허용되지 않은 이미지 포맷입니다.",
              "errors": []
          }"""

        const val UPLOAD_IO_ERROR = """{
              "code": "UPL-0006",
              "message": "파일 처리 중 오류가 발생했습니다.",
              "errors": []
          }"""

        const val INVALID_INPUT = """{
              "code": "V-0001",
              "message": "요청 값이 올바르지 않습니다.",
              "errors": []
          }"""
    }
}