package kr.co.wground.track.docs

object TrackSwaggerErrorExample {
    object NotFound {
        const val TRACK_NOT_FOUND = """{
                 "code":"T-0001",
                 "message":"해당 과정을 찾을 수 없습니다.",
                 "errors":[]
            }"""
    }

    object BadRequest {
        const val INVALID_DATE_RANGE = """{
                "code":"T-0002",
                "message":"시작 일자는 종료 일자이후가 될 수 없습니다.",
                "errors":[]
            }"""

        const val INVALID_TRACK_INPUT = """{
                "code":"T-0004",
                "message":"트랙 입력값이 올바르지 않습니다.",
                "errors":[]
            }"""
    }
}
