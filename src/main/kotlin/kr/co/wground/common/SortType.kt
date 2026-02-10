package kr.co.wground.common

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import kr.co.wground.study.domain.QStudy.study
import org.springframework.data.domain.Sort


enum class SortType(val sortDirection: Sort) {
    DESC(Sort.by(Sort.Direction.DESC, "createdAt")){
        override fun getOrderSpecifier() = OrderSpecifier(Order.DESC, study.createdAt)
    },
    ASC(Sort.by(Sort.Direction.ASC, "createdAt")){
        override fun getOrderSpecifier() = OrderSpecifier(Order.ASC, study.createdAt)
    };

    abstract fun getOrderSpecifier(): OrderSpecifier<*>

    companion object {
        fun from(value: String?): SortType {
            return SortType.entries.find { it.name.equals(value, ignoreCase = true) } ?: DESC
        }
    }
}