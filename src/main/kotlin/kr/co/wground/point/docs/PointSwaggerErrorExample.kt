object PointSwaggerErrorExample {
    object NotFound {
        const val WALLET_NOT_FOUND = """{
              "code": "PT-0001",
              "message": "포인트 지갑을 찾을 수 없습니다.",
              "errors": []
          }"""
    }

    object BadRequest {
        const val INVALID_USER_ID = """{
              "code": "PT-0005",
              "message": "유효하지 않은 사용자 ID입니다.",
              "errors": []
          }"""

        const val INVALID_AMOUNT = """{
              "code": "PT-0006",
              "message": "포인트 금액은 0보다 커야 합니다.",
              "errors": []
          }"""

        const val INVALID_INPUT = """{
              "code": "V-0001",
              "message": "요청 값이 올바르지 않습니다.",
              "errors": []
          }"""
    }

    object Forbidden {
        const val ACCESS_DENIED = """{
              "code": "A-0001",
              "message": "접근 권한이 없습니다.",
              "errors": []
          }"""
    }

    object ServerError {
        const val POINT_PROCESSING_FAILED = """{
              "code": "PT-0011",
              "message": "포인트 처리 중 오류가 발생했습니다.",
              "errors": []
          }"""
    }
}