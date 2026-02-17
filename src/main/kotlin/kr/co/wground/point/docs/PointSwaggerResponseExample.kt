package kr.co.wground.point.docs

object PointSwaggerResponseExample {

    const val BALANCE = """{
          "balance": 1250,
          "lastUpdatedAt": "2026-02-17T10:30:00"
      }"""

    const val HISTORIES = """{
          "histories": [
              {
                  "id": 1,
                  "amount": 50,
                  "type": "WRITE_POST",
                  "description": "게시글 작성",
                  "refType": "POST",
                  "refId": 10,
                  "createdAt": "2026-02-17T10:30:00"
              },
              {
                  "id": 2,
                  "amount": 10,
                  "type": "ATTENDANCE",
                  "description": "출석 체크",
                  "refType": "ATTENDANCE",
                  "refId": 20260217,
                  "createdAt": "2026-02-17T09:00:00"
              },
              {
                  "id": 3,
                  "amount": 500,
                  "type": "USE_SHOP",
                  "description": "상점 아이템 구매",
                  "refType": "SHOP_ITEM",
                  "refId": 3,
                  "createdAt": "2026-02-16T14:20:00"
              },
              {
                  "id": 4,
                  "amount": 300,
                  "type": "EVENT_ADMIN",
                  "description": "이벤트 지급",
                  "refType": "EVENT",
                  "refId": 1,
                  "createdAt": "2026-02-15T11:00:00"
              }
          ],
          "hasNext": true
      }"""

    const val HISTORIES_EMPTY = """{
          "histories": [],
          "hasNext": false
      }"""
}