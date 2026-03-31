package kr.co.wground.track.docs

object TrackSwaggerRequestExample {
    const val CREATE_TRACK = """{
      "trackType": "FE",
      "cardinal": 3,
      "startDate": "2026-03-01",
      "endDate": "2026-08-31"
    }"""

    const val UPDATE_TRACK = """{
      "trackType": "AI",
      "cardinal": 5,
      "startDate": "2026-04-01",
      "endDate": "2026-09-30"
    }"""
}
