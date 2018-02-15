package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.model.Authority
import fr.edjaz.domain.model.User
import fr.edjaz.web.service.security.SecurityUtils
import fr.edjaz.web.service.user.response.AuthorityResponse
import fr.edjaz.web.service.user.response.UserModelResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class GetUserwithAuthoritiesImpl(
    private val userGateway: UserGateway
) : GetUserWithAuthorities {
    private val log = LoggerFactory.getLogger(this::class.java)
    @Transactional(readOnly = true)
    override fun execute(): Optional<UserModelResponse> = SecurityUtils.getCurrentUserLogin().flatMap({ userGateway.findOneWithAuthoritiesByLogin(it) }).map { it.toResponse() }

    @Transactional(readOnly = true)
    override fun execute(id: Long?): Optional<UserModelResponse> = userGateway.findOneWithAuthoritiesById(id).map { it.toResponse() }
}

fun User.toResponse(): UserModelResponse {
    return UserModelResponse(
        id, login, password, firstName, lastName, email, activated, langKey, imageUrl, activationKey, resetKey, resetDate, authorities.toListResponse(),createdBy,createdDate,lastModifiedBy,lastModifiedDate
    )
}
fun Authority.toResponse() : AuthorityResponse = AuthorityResponse(name)
fun Set<Authority>.toListResponse() : Set<AuthorityResponse> = map { it.toResponse() }.toSet()
