package fr.edjaz.web.service.social

import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.web.service.mail.SendSocialRegistrationValidationEmail
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.social.connect.Connection
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.stereotype.Service

@Service
class DeleteUserSocialConnectionImpl(
    private val usersConnectionRepository: UsersConnectionRepository
) : DeleteUserSocialConnection{

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(login: String) {
        val connectionRepository = usersConnectionRepository.createConnectionRepository(login)
        connectionRepository.findAllConnections().keys.stream()
            .forEach { providerId ->
                connectionRepository.removeConnections(providerId)
                log.debug("Delete user social connection providerId: {}", providerId)
            }
    }

}
