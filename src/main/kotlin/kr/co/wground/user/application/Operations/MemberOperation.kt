package kr.co.wground.user.application.Operations

import kr.co.wground.user.infra.UserRepository

class MemberOperation(
    val userRepository: UserRepository
): UserOperations {

}
