package kr.co.wground.util.db

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Path
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.Expressions
import kr.co.wground.exception.BusinessException
import kr.co.wground.exception.CommonErrorCode
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.PropertyPath
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.util.Assert

class OrderSpecifierTransformer(private val base: EntityPathBase<*>) {
    fun transform(order: Sort.Order): OrderSpecifier<*> {
        @Suppress("UNCHECKED_CAST")
        return OrderSpecifier(
            if (order.isAscending) Order.ASC else Order.DESC,
            buildOrderPropertyPathFrom(order) as Expression<Comparable<*>>,
            toQueryDslNullHandling(order.nullHandling)
        )
    }

    fun transform(sort: Sort): Array<OrderSpecifier<*>> {
        return sort.map { transform(it) }.toList().toTypedArray()
    }

    private fun toQueryDslNullHandling(nullHandling: Sort.NullHandling): OrderSpecifier.NullHandling {
        Assert.notNull(nullHandling, "NullHandling must not be null!")
        return when (nullHandling) {
            Sort.NullHandling.NULLS_FIRST -> OrderSpecifier.NullHandling.NullsFirst
            Sort.NullHandling.NULLS_LAST -> OrderSpecifier.NullHandling.NullsLast
            Sort.NullHandling.NATIVE -> OrderSpecifier.NullHandling.Default
            else -> OrderSpecifier.NullHandling.Default
        }
    }

    private fun buildOrderPropertyPathFrom(order: Sort.Order): Expression<*>? {
        Assert.notNull(order, "Order must not be null!")

        var path: PropertyPath? = try {
            PropertyPath.from(order.property, base.type)
        } catch (e: PropertyReferenceException) {
            throw BusinessException(CommonErrorCode.ORDER_BY_PROPERTY_NOT_FOUND)
        }
        var sortPropertyExpression: Expression<*>? = base
        while (null != path) {
            sortPropertyExpression = if (!path.hasNext() && order.isIgnoreCase) {
                Expressions.stringPath(sortPropertyExpression as Path<*>?, path.segment).lower()
            } else {
                Expressions.path(path.type, sortPropertyExpression as Path<*>?, path.segment)
            }
            path = path.next()
        }
        return sortPropertyExpression
    }
}
