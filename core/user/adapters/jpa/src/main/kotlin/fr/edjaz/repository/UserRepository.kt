package fr.edjaz.repository

import fr.edjaz.domain.AuthorityEntity
import fr.edjaz.domain.UserEntity
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.model.Authority
import fr.edjaz.domain.model.User
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findOneByActivationKey(activationKey: String): Optional<UserEntity>

    fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<UserEntity>

    fun findOneByResetKey(resetKey: String): Optional<UserEntity>

    fun findOneByEmailIgnoreCase(email: String): Optional<UserEntity>

    fun findOneByLogin(login: String): Optional<UserEntity>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesById(id: Long?): Optional<UserEntity>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    @Cacheable(cacheNames = arrayOf(UserGateway.USERS_BY_LOGIN_CACHE))
    fun findOneWithAuthoritiesByLogin(login: String): Optional<UserEntity>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    @Cacheable(cacheNames = arrayOf(UserGateway.USERS_BY_EMAIL_CACHE))
    fun findOneWithAuthoritiesByEmail(email: String): Optional<UserEntity>

    fun findAllByLoginNot(pageable: Pageable, login: String): Page<UserEntity>


}

@Component
class UserGatewayImpl(private val userRepository: UserRepository) : UserGateway {
    override fun findAll(): List<User> = userRepository.findAll().toDomainList()

    override fun saveAndFlush(user: User): User = userRepository.saveAndFlush(user.toEntity()).toDomain()

    override fun findOneByActivationKey(activationKey: String): Optional<User> = userRepository.findOneByActivationKey(activationKey).toDomain()

    override fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<User> = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(dateTime).toDomainList()

    override fun findOneByResetKey(resetKey: String): Optional<User> = userRepository.findOneByResetKey(resetKey).toDomain()

    override fun findOneByEmailIgnoreCase(email: String): Optional<User> = userRepository.findOneByEmailIgnoreCase(email).toDomain()

    override fun findOneByLogin(login: String): Optional<User> = userRepository.findOneByLogin(login).toDomain()

    override fun findOneWithAuthoritiesById(id: Long?): Optional<User> = userRepository.findOneWithAuthoritiesById(id).toDomain()

    override fun findOneWithAuthoritiesByLogin(login: String): Optional<User> = userRepository.findOneWithAuthoritiesByLogin(login).toDomain()

    override fun findOneWithAuthoritiesByEmail(email: String): Optional<User> = userRepository.findOneWithAuthoritiesByEmail(email).toDomain()

    override fun findAllByLoginNot(pageable: Pageable, login: String): Page<User> = userRepository.findAllByLoginNot(pageable, login).toDomain()

    override fun save(user: User): User = userRepository.save(user.toEntity()).toDomain()

    override fun findOne(id: Long?): User = userRepository.findOne(id).toDomain()

    override fun delete(user: User) {
        userRepository.delete(user.toEntity())
    }

}


fun List<UserEntity>.toDomainList(): List<User> = this.map { it.toDomain() }
fun Optional<UserEntity>.toDomain(): Optional<User> = this.map { it.toDomain() }
fun UserEntity.toDomain(): User = User(id, login, password, firstName, lastName, email, activated, langKey, imageUrl, activationKey, resetKey, resetDate, authorities.toDomainList())
fun Set<AuthorityEntity>.toDomainList(): Set<Authority> = map { it.toDomain() }.toSet()
fun Page<UserEntity>.toDomain(): Page<User> = this.map { it.toDomain() }
fun User.toEntity(): UserEntity {
    var user = UserEntity()
    user.id = id
    user.activated = activated
    user.activationKey = activationKey
    user.authorities = authorities.toEntityList()
    user.email = email
    user.firstName = firstName
    user.imageUrl = imageUrl
    user.langKey = langKey
    user.lastName = lastName
    user.login = login
    user.password = password
    user.resetDate = resetDate
    user.resetKey = resetKey

    return user
}

fun Set<Authority>.toEntityList(): Set<AuthorityEntity> = this.map { it.toEntity() }.toSet()
fun Authority.toEntity(): AuthorityEntity = AuthorityEntity(name)
