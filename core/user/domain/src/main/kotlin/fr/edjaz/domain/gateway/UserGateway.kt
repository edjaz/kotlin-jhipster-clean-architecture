package fr.edjaz.domain.gateway

import fr.edjaz.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.*

/**
 * Spring Data JPA repository for the User entity.
 */
interface UserGateway {

    fun findOneByActivationKey(activationKey: String): Optional<User>

    fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<User>

    fun findOneByResetKey(resetKey: String): Optional<User>

    fun findOneByEmailIgnoreCase(email: String): Optional<User>

    fun findOneByLogin(login: String): Optional<User>

    fun findOneWithAuthoritiesById(id: Long?): Optional<User>

    fun findOneWithAuthoritiesByLogin(login: String): Optional<User>

    fun findOneWithAuthoritiesByEmail(email: String): Optional<User>

    fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>

    fun save(user: User): User
    fun findOne(id: Long?): User
    fun delete(user: User)
    fun saveAndFlush(user: User): User
    fun findAll(): List<User>

    companion object {

        const val USERS_BY_LOGIN_CACHE = "usersByLogin"

        const val USERS_BY_EMAIL_CACHE = "usersByEmail"
    }
}
