package kr.co.wground.track.docs

object TrackSwaggerResponseExample {
    const val TRACK_LIST = """{
             "content": [
                 {
                     "trackId": 1,
                     "trackName": "운영자",
                     "startDate": "2025-01-01",
                     "endDate": "2025-06-30",
                    "trackStatus": "GRADUATED"
                },
                {
                    "trackId": 2,
                    "trackName": "FE 1기",
                    "startDate": "2026-01-01",
                    "endDate": "2026-06-30",
                    "trackStatus": "ENROLLED"
                }
            ]
        }"""

    const val TRACK_LIST_EXCEPT_ADMIN = """{
             "content": [
                 {
                     "trackId": 2,
                     "trackName": "BE 1기",
                     "startDate": "2026-01-01",
                     "endDate": "2026-06-30",
                    "trackStatus": "ENROLLED"
                },
                {
                    "trackId": 3,
                    "trackName": "FE 1기",
                    "startDate": "2026-01-01",
                    "endDate": "2026-06-30",
                    "trackStatus": "ENROLLED"
                }
            ]
        }"""

    const val TRACK_CARDINALS = """{
             "trackType": "FE",
             "cardinals": [1, 2, 4]
        }"""

    const val SIGNUP_TRACK_TYPES = """{
             "trackTypes": [
                 {
                     "trackType": "BE",
                     "label": "BE",
                     "requiresCardinal": true
                 },
                 {
                     "trackType": "FE",
                     "label": "FE",
                     "requiresCardinal": true
                 },
                 {
                     "trackType": "AI",
                     "label": "AI Agent",
                     "requiresCardinal": true
                 }
             ]
        }"""

    const val SIGNUP_TRACK_RESOLVE = """{
             "trackType": "FE",
             "cardinal": 3,
             "trackId": 12
        }"""

    const val TRACK_TYPES = """{
             "trackTypes": [
                 { "trackType": "BE", "label": "BE", "requiresCardinal": true },
                 { "trackType": "FE", "label": "FE", "requiresCardinal": true },
                 { "trackType": "AI", "label": "AI Agent", "requiresCardinal": true },
                 { "trackType": "UNREAL", "label": "언리얼", "requiresCardinal": true },
                 { "trackType": "GAME", "label": "게임", "requiresCardinal": true }
             ]
        }"""
}
