package kr.co.wground.shop.application.query

import kr.co.wground.exception.BusinessException
import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.application.dto.ShopItemSummaryDto
import kr.co.wground.shop.domain.constant.ShopItemStatus
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

    override fun getActiveItems(): List<ShopItemSummaryDto> {
        return shopItemRepository.findSummariesByStatus(ShopItemStatus.ACTIVE)
    }

    override fun getShopItemDetail(id: Long): ShopItemDetailDto {
        val shopItem = shopItemRepository.findByIdOrNull(id)
            ?: throw BusinessException(ShopErrorCode.ITEM_NOT_FOUND)
        return ShopItemDetailDto.from(shopItem)
    }
}