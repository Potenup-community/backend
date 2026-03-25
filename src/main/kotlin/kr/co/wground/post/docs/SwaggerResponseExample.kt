package kr.co.wground.post.docs

object SwaggerResponseExample {
    const val POST_SUMMARY_RESPONSE = """
    {
      "contents": [
        {
          "postId": 1,
          "title": "첫 번째 게시글",
          "writerId": 10,
          "writerName": "홍길동",
          "wroteAt": "2026-01-07T14:30:00",
          "topic": "NOTICE",
          "highlightType": "BY_ADMIN",
          "commentsCount": 3,
          "reactions": [
            {
              "reactionType": "LIKE",
              "count": 5,
              "reactedByMe": true
            },
            {
              "reactionType": "HEART",
              "count": 1,
              "reactedByMe": false
            }
          ]
        }
      ],
      "items": [
            {
              "itemType": "PET",
              "imageUrl": "/api/v1/shop/items/2/image"
            },
            {
              "itemType": "FRAME",
              "imageUrl": "/api/v1/shop/items/3/image"
            },
            {
              "itemType": "BADGE",
              "imageUrl": "/api/v1/shop/items/5/image"
            }
          ]
      "hasNext": true,
      "nextPage": 2
    }
    """

    const val POST_DETAIL_RESPONSE = """
    {
      "postId": 2,
      "writerId": 10,
      "writerName": "홍길동",
      "title": "공지사항 제목",
      "content": "공지사항 본문입니다.",
      "topic": "NOTICE",
      "highlightType": "BY_ADMIN",
      "commentsCount": 5,
      "wroteAt": "2026-01-07T15:00:00",
      "previousPost": {
        "previousPostId": 2,
        "previousPostTitle": "Example Title"
      },
      "nextPost": {
        "nextPostId": null,
        "nextPostTitle": null
      },
      "reactions": [
        {
          "reactionType": "LIKE",
          "count": 12
        },
        {
          "reactionType": "HEART",
          "count": 1
        }
      ],
      "items": [
            {
              "itemType": "PET",
              "imageUrl": "/api/v1/shop/items/2/image"
            },
            {
              "itemType": "FRAME",
              "imageUrl": "/api/v1/shop/items/3/image"
            },
            {
              "itemType": "BADGE",
              "imageUrl": "/api/v1/shop/items/5/image"
            }
          ]
    }
    """
}
