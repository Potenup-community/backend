package kr.co.wground.user.application.operations

import kr.co.wground.user.application.operations.dto.AdminSearchUserDto
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.application.operations.dto.DecisionDto
import kr.co.wground.user.application.operations.dto.UserConditionCountDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AdminService {
    fun decisionSignup(decisionDto: DecisionDto)
    fun findUsersByConditions(conditionDto: ConditionDto, pageable: Pageable): Page<AdminSearchUserDto>
    fun countUserWithCondition(conditionDto: ConditionDto): UserConditionCountDto
}
