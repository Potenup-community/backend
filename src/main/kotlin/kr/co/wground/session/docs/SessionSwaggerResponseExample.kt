package kr.co.wground.session.docs

object SessionSwaggerResponseExample {

    const val SESSION_LIST = """{
        "sessions": [
            {
                "sessionId": "550e8400-e29b-41d4-a716-446655440000",
                "deviceName": "Chrome on Mac",
                "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
                "ipAddress": "192.168.1.1",
                "createdAt": "2026-03-22T10:00:00",
                "lastSeenAt": "2026-03-23T09:30:00",
                "isCurrent": true
            },
            {
                "sessionId": "661f9511-f30c-52e5-b827-557766551111",
                "deviceName": "Safari on iPhone",
                "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)",
                "ipAddress": "192.168.1.2",
                "createdAt": "2026-03-21T08:00:00",
                "lastSeenAt": "2026-03-22T20:00:00",
                "isCurrent": false
            }
        ]
    }"""
}
