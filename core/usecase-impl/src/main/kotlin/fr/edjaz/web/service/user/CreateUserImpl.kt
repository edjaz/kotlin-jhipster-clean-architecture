package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.domain.model.Authority
import fr.edjaz.domain.model.User
import fr.edjaz.domain.config.Constants
import fr.edjaz.web.service.rest.errors.BadRequestAlertException
import fr.edjaz.web.service.rest.errors.EmailAlreadyUsedException
import fr.edjaz.web.service.rest.errors.LoginAlreadyUsedException
import fr.edjaz.web.service.user.request.CreateUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse
import fr.edjaz.web.service.util.RandomUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.stream.Collectors

@Service
@Transactional
class CreateUserImpl(    private val userGateway: UserGateway
                          , private val authorityGateway: AuthorityGateway
                          , private val userSearchGateway: UserSearchGateway
                          , private val passwordEncoder: PasswordEncoder
                          , private val cacheManager: CacheManager
) : CreateUser {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(request: CreateUserRequest): UserModelResponse {
        if (request.id != null) {
            throw BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists")
            // Lowercase the user login before comparing with database
        } else if (userGateway.findOneByLogin(request.login!!.toLowerCase()).isPresent) {
            throw LoginAlreadyUsedException()
        } else if (userGateway.findOneByEmailIgnoreCase(request.email!!).isPresent) {
            throw EmailAlreadyUsedException()
        }

        val user = User()
        user.login = request.login
        user.firstName = request.firstName
        user.lastName = request.lastName
        user.email = request.email
        user.imageUrl = request.imageUrl
        if (request.langKey == null) {
            user.langKey = Constants.DEFAULT_LANGUAGE // default language
        } else {
            user.langKey = request.langKey
        }
        if (request.authorities != null) {
            val authorities = request.authorities!!.stream()
                .map<Authority>({ authorityGateway.findOne(it) })
                .collect(Collectors.toSet())
            user.authorities = authorities
        }
        val encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword())
        user.password = encryptedPassword
        user.resetKey = RandomUtil.generateResetKey()
        user.resetDate = Instant.now()
        user.activated = true
        userGateway.save(user)
        userSearchGateway.save(user)
        cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
        cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
        log.debug("Created Information for User: {}", user)
        return user.toResponse()
    }
}
