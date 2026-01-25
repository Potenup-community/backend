package kr.co.wground.notification.docs

object NotificationSwaggerErrorExample {
    object NotFound {
        const val NOTIFICATION = """
        {
            "code": "N-0006",
            "message": "알림을 찾을 수 없습니다."
        }
        """
    }
}

object NotificationSwaggerResponseExample {
    const val NOTIFICATIONS = """
    {
        "notifications": [
            {
                "id": 1,
                "type": "POST_COMMENT",
                "title": "새 댓글",
                "content": "회원님의 게시글에 댓글이 달렸습니다.",
                "actorId": 2,
                "referenceType": "POST",
                "referenceId": 10,
                "status": "UNREAD",
                "createdAt": "2025-01-25T10:30:00"
            },
            {
                "id": 2,
                "type": "POST_REACTION",
                "title": "게시글 좋아요",
                "content": "회원님의 게시글에 좋아요가 눌렸습니다.",
                "actorId": 3,
                "referenceType": "POST",
                "referenceId": 10,
                "status": "READ",
                "createdAt": "2025-01-25T09:00:00"
            }
        ],
        "hasNext": true
    }
    """

    const val UNREAD_COUNT = """
    {
        "count": 5
    }
    """
}
