package kr.co.wground.shop.application.query

import kotlin.collections.orEmpty
import kr.co.wground.exception.BusinessException
import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.presentation.response.ShopItemGroupResponse
import kr.co.wground.shop.domain.constant.ShopItemStatus
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.exception.ShopErrorCode
import kr.co.wground.shop.infra.item.ShopItemRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ShopQueryService(
    private val shopItemRepository: ShopItemRepository,
) : ShopQueryUseCase {

    override fun getActiveItems(): List<ShopItemGroupResponse> {
        val shopItems =shopItemRepository.findSummariesByStatus(ShopItemStatus.ACTIVE)

        val grouped = shopItems.groupBy { it.itemType }

        return ShopItemType.entries.map { type ->
            ShopItemGroupResponse(
                itemType = type,
                items = grouped[type].orEmpty()
            )
        }
    }

    override fun getShopItemDetail(id: Long): ShopItemDetailDto {
        val shopItem = shopItemRepository.findByIdOrNull(id)
            ?: throw BusinessException(ShopErrorCode.ITEM_NOT_FOUND)
        return ShopItemDetailDto.from(shopItem)
    }
}