package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.domain.model.Authority
import fr.edjaz.domain.model.User
import fr.edjaz.web.service.rest.errors.EmailAlreadyUsedException
import fr.edjaz.web.service.rest.errors.LoginAlreadyUsedException
import fr.edjaz.web.service.security.AuthoritiesConstants
import fr.edjaz.web.service.user.request.RegisterUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse
import fr.edjaz.web.service.util.RandomUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.HashSet

@Service
@Transactional
class RegisterUserImpl(    private val userGateway: UserGateway
                           , private val authorityGateway: AuthorityGateway
                           , private val userSearchGateway: UserSearchGateway
                           , private val passwordEncoder: PasswordEncoder
                           , private val cacheManager: CacheManager
) : RegisterUser {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(request: RegisterUserRequest): UserModelResponse {

        userGateway.findOneByLogin(request.login!!.toLowerCase()).ifPresent { throw LoginAlreadyUsedException() }
        userGateway.findOneByEmailIgnoreCase(request.email!!).ifPresent { throw EmailAlreadyUsedException() }

        val newUser = User()
        val authority = authorityGateway.findOne(AuthoritiesConstants.USER)
        var authorities = HashSet<Authority>()
        val encryptedPassword = passwordEncoder.encode(request.password)
        newUser.login = request.login
        // new user gets initially a generated password
        newUser.password = encryptedPassword
        newUser.firstName = request.firstName
        newUser.lastName = request.lastName
        newUser.email = request.email
        newUser.imageUrl = request.imageUrl
        newUser.langKey = request.langKey
        // new user is not active
        newUser.activated = false
        // new user gets registration key
        newUser.activationKey = RandomUtil.generateActivationKey()
        authorities.add(authority)
        newUser.authorities = authorities
        userGateway.save(newUser)
        userSearchGateway.save(newUser)
        cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(newUser.login)
        cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(newUser.email)
        log.debug("Created Information for User: {}", newUser)
        return newUser.toResponse()
    }
}

