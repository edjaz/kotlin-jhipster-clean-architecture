package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.web.service.user.response.UserModelResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserSearchImpl(private val userSearchGateway: UserSearchGateway) : UserSearch{
    override fun execute(query: String): List<UserModelResponse> {
        return userSearchGateway.search(query)!!.map { it.toResponse() }
    }
}
