package kr.co.wground.shop.docs

object ShopSwaggerResponseExample {

    const val CREATED_ITEM = """{
          "id": 1,
          "name": "골드 뱃지",
          "description": "커뮤니티 활동 우수자에게 부여되는 골드 뱃지입니다.",
          "price": 500,
          "itemType": "BADGE",
          "consumable": false,
          "durationDays": null,
          "imageUrl": "/images/items/a1b2c3d4-e5f6-7890-abcd-ef1234567890.png"
      }"""

    const val CREATED_CONSUMABLE_ITEM = """{
          "id": 2,
          "name": "30일 프로필 프레임",
          "description": "30일간 사용 가능한 특별 프로필 프레임입니다.",
          "price": 200,
          "itemType": "FRAME",
          "consumable": true,
          "durationDays": 30,
          "imageUrl": "/images/items/b2c3d4e5-f6a7-8901-bcde-f12345678901.gif"
      }"""

    const val UPDATED_ITEM = """{
          "id": 1,
          "name": "골드 뱃지 (리뉴얼)",
          "description": "리뉴얼된 골드 뱃지입니다.",
          "price": 600,
          "itemType": "BADGE",
          "consumable": false,
          "durationDays": null,
          "imageUrl": "/images/items/c3d4e5f6-a7b8-9012-cdef-123456789012.png"
      }"""

    const val SHOP_ITEMS = """                                                                                                                                                                                                                                                                                        
      [                                                                                                                                                                                                                                                                                                                 
        {
          "itemType": "BADGE",
          "items": [
            {
              "id": 1,
              "name": "골드 뱃지",
              "price": 500,
              "itemType": "BADGE",
              "consumable": false,
              "durationDays": null,
              "imageUrl": "/images/items/a1b2c3d4.png"
            }
          ]
        },
        {
          "itemType": "PET",
          "items": []
        },
        {
          "itemType": "FRAME",
          "items": [
            {
              "id": 2,
              "name": "30일 프로필 프레임",
              "price": 200,
              "itemType": "FRAME",
              "consumable": true,
              "durationDays": 30,
              "imageUrl": "/images/items/b2c3d4e5.gif"
            }
          ]
        }
      ]
      """

        const val SHOP_ITEM_DETAIL = """{
        "id": 1,
        "name": "골드 뱃지",
        "description": "커뮤니티 활동 우수자에게 부여되는 골드 뱃지입니다.",
        "price": 500,
        "itemType": "BADGE",
        "consumable": false,
        "durationDays": null,
        "imageUrl": "/images/items/a1b2c3d4.png"
      }"""

        const val MY_INVENTORY = """
      [
        {
          "itemType": "BADGE",
          "items": [
            {
              "inventoryId": 10,
              "shopItemId": 1,
              "name": "골드 뱃지",
              "description": "커뮤니티 활동 우수자에게 부여되는 골드 뱃지입니다.",
              "itemType": "BADGE",
              "imageUrl": "/images/items/a1b2c3d4.png",
              "consumable": false,
              "equipped": true,
              "remainingDays": null
            }
          ]
        },
        {
          "itemType": "PET",
          "items": []
        },
        {
          "itemType": "FRAME",
          "items": [
            {
              "inventoryId": 11,
              "shopItemId": 2,
              "name": "30일 프로필 프레임",
              "description": "30일간 사용 가능한 특별 프로필 프레임입니다.",
              "itemType": "FRAME",
              "imageUrl": "/images/items/b2c3d4e5.gif",
              "consumable": true,
              "equipped": false,
              "remainingDays": 23
            }
          ]
        }
      ]
      """
}