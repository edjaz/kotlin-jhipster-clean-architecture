package fr.edjaz.web.service.user

import fr.edjaz.domain.config.Constants
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.web.service.user.response.UserModelResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GetAllManagedUsersImpl(private val userGateway: UserGateway) : GetAllManagedUsers {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(readOnly = true)
    override fun execute(pageable: Pageable): Page<UserModelResponse> {
        return userGateway.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map({ it.toResponse() })
    }
}
