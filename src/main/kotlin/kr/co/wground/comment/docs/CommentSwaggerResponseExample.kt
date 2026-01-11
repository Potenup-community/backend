package kr.co.wground.comment.docs

object CommentSwaggerResponseExample {
    const val COMMENTS = """
    [
        {
            "commentId": 1,
            "content": "test content",
            "author": {
                "userId": 1,
                "name": "홍길동",
                "trackName": "BE 1기",
                "profileImageUrl": "/api/v1/users/profiles/1"
            },
            "createdAt": "2025-12-21T20:24:34.95517",
            "reactionCount": 4,
            "isDeleted": false,
            "replies": [
                {
                    "commentId": 2,
                    "content": "sub test content",
                    "author": {
                        "userId": 3,
                        "name": "김철수",
                        "trackName": "FE 1기",
                        "profileImageUrl": "/api/v1/users/profiles/3"
                    },
                    "createdAt": "2025-12-21T20:26:07.370003",
                    "reactionCount": 1,
                    "isDeleted": false,
                    "replies": []
                }
            ]
        }
]
    """
}
