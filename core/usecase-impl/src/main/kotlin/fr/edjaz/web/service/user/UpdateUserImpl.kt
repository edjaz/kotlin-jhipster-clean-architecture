package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.domain.model.User
import fr.edjaz.web.service.rest.errors.EmailAlreadyUsedException
import fr.edjaz.web.service.rest.errors.InternalServerErrorException
import fr.edjaz.web.service.rest.errors.LoginAlreadyUsedException
import fr.edjaz.web.service.security.SecurityUtils
import fr.edjaz.web.service.user.request.FullUpdateUserRequest
import fr.edjaz.web.service.user.request.UpdateUserRequest
import fr.edjaz.web.service.user.response.UserModelResponse
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UpdateUserImpl(    private val userGateway: UserGateway
                          , private val authorityGateway: AuthorityGateway
                          , private val userSearchGateway: UserSearchGateway
                          , private val passwordEncoder: PasswordEncoder
                          , private val cacheManager: CacheManager
) : UpdateUser {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(request: UpdateUserRequest) {
        val userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow { InternalServerErrorException("Current user login not found") }
        val existingUser = userGateway.findOneByEmailIgnoreCase(request.email!!)
        if (existingUser.isPresent && !existingUser.get().login!!.equals(userLogin, ignoreCase = true)) {
            throw EmailAlreadyUsedException()
        }
        val user = userGateway.findOneByLogin(userLogin)
        if (!user.isPresent) {
            throw InternalServerErrorException("User could not be found")
        }

        SecurityUtils.getCurrentUserLogin()
            .flatMap<User>({ userGateway.findOneByLogin(it) })
            .ifPresent { user ->
                user.firstName = request.firstName
                user.lastName = request.lastName
                user.email = request.email
                user.langKey = request.langKey

                user.imageUrl = request.imageUrl
                userGateway.save(user)
                userSearchGateway.save(user)
                cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
                cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
                log.debug("Changed Information for User: {}", user)
            }
    }

    override fun execute(request: FullUpdateUserRequest): Optional<UserModelResponse> {

        var existingUser = userGateway.findOneByEmailIgnoreCase(request.email!!)
        if (existingUser.isPresent && existingUser.get().id != request.id) {
            throw EmailAlreadyUsedException()
        }
        existingUser = userGateway.findOneByLogin(request.login!!.toLowerCase())
        if (existingUser.isPresent && existingUser.get().id != request.id) {
            throw LoginAlreadyUsedException()
        }

        return Optional.of(userGateway
            .findOne(request.id))
            .map { user ->
                user.login = request.login
                user.firstName = request.firstName
                user.lastName = request.lastName
                user.email = request.email
                user.imageUrl = request.imageUrl
                user.activated = request.isActivated
                user.langKey = request.langKey

                var managedAuthorities = user.authorities.toHashSet()
                managedAuthorities.clear()
                request.authorities!!.stream()
                    .map { authorityGateway.findOne(it) }
                    .forEach({
                        managedAuthorities.add(it!!)
                    })

                //user.authorities = managedAuthorities;

                userGateway.save(user)
                userSearchGateway.save(user)
                cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
                cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
                log.debug("Changed Information for User: {}", user)
                user.toResponse()
            }
    }
}
