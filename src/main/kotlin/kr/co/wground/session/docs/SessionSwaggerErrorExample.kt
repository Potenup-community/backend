package kr.co.wground.session.docs

object SessionSwaggerErrorExample {

    object NotFound {
        const val SESSION_NOT_FOUND = """{
            "code": "SS-0001",
            "message": "세션을 찾을 수 없습니다.",
            "errors": []
        }"""
    }

    object Forbidden {
        const val SESSION_FORBIDDEN = """{
            "code": "SS-0002",
            "message": "해당 세션에 접근 권한이 없습니다.",
            "errors": []
        }"""
    }

    object Unauthorized {
        const val SESSION_INACTIVE = """{
            "code": "SS-0003",
            "message": "비활성 세션입니다. 다시 로그인해 주세요.",
            "errors": []
        }"""
    }

    object BadRequest {
        const val SESSION_ALREADY_INACTIVE = """{
            "code": "SS-0004",
            "message": "이미 비활성화된 세션입니다.",
            "errors": []
        }"""
    }
}
