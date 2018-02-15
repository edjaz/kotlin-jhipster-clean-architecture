package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.web.service.security.SecurityUtils
import fr.edjaz.web.service.user.response.UserModelResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class GetUserWithAuthoritiesByLoginImpl(private val userGateway: UserGateway) : GetUserWithAuthoritiesByLogin {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(readOnly = true)
    override fun execute(login: String): Optional<UserModelResponse> {
        return userGateway.findOneWithAuthoritiesByLogin(login).map { it.toResponse() }
    }
}
