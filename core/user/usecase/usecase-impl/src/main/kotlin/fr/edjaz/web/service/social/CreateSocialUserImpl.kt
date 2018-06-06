package fr.edjaz.web.service.social

import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.domain.model.Authority
import fr.edjaz.domain.model.User
import fr.edjaz.web.service.mail.SendSocialRegistrationValidationEmail
import fr.edjaz.web.service.mail.request.MailUser
import fr.edjaz.web.service.mail.request.SendSocialRegistrationValidationEmailRequest
import fr.edjaz.web.service.security.AuthoritiesConstants
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.social.connect.Connection
import org.springframework.social.connect.UserProfile
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class CreateSocialUserImpl(
    private val usersConnectionRepository: UsersConnectionRepository
    , private val authorityGateway: AuthorityGateway
    , private val passwordEncoder: PasswordEncoder
    , private val userGateway: UserGateway
    , private val sendSocialRegistrationValidationEmail: SendSocialRegistrationValidationEmail
    , private val userSearchRepository: UserSearchGateway
) : CreateSocialUser{

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(connection: Connection<*>?, langKey: String) {
        if (connection == null) {
            log.error("Cannot create social user because connection is null")
            throw IllegalArgumentException("Connection cannot be null")
        }
        val userProfile = connection.fetchUserProfile()
        val providerId = connection.key.providerId
        val imageUrl = connection.imageUrl
        val user = createUserIfNotExist(userProfile, langKey, providerId, imageUrl)
        createSocialConnection(user.login, connection)
        sendSocialRegistrationValidationEmail.execute(SendSocialRegistrationValidationEmailRequest(providerId, user.toMailUser()))
    }

    fun User.toMailUser(): MailUser = MailUser(id, login, password, firstName, lastName, email, activated, langKey, imageUrl, activationKey, resetKey)

    private fun createUserIfNotExist(userProfile: UserProfile, langKey: String, providerId: String, imageUrl: String): User {
        val email = userProfile.email
        var userName = userProfile.username
        if (!StringUtils.isBlank(userName)) {
            userName = userName.toLowerCase(Locale.ENGLISH)
        }
        if (StringUtils.isBlank(email) && StringUtils.isBlank(userName)) {
            log.error("Cannot create social user because email and login are null")
            throw IllegalArgumentException("Email and login cannot be null")
        }
        if (StringUtils.isBlank(email) && userGateway.findOneByLogin(userName).isPresent) {
            log.error("Cannot create social user because email is null and login already exist, login -> {}", userName)
            throw IllegalArgumentException("Email cannot be null with an existing login")
        }
        if (!StringUtils.isBlank(email)) {
            val user = userGateway.findOneByEmailIgnoreCase(email)
            if (user.isPresent) {
                log.info("User already exist associate the connection to this account")
                return user.get()
            }
        }

        val login = getLoginDependingOnProviderId(userProfile, providerId)
        val encryptedPassword = passwordEncoder.encode(RandomStringUtils.random(10))
        val authorities = HashSet<Authority>(1)
        authorities.add(authorityGateway.findOne(AuthoritiesConstants.USER))

        val newUser = User()
        newUser.login = login
        newUser.password = encryptedPassword
        newUser.firstName = userProfile.firstName
        newUser.lastName = userProfile.lastName
        newUser.email = email
        newUser.activated = true
        newUser.authorities = authorities
        newUser.langKey = langKey
        newUser.imageUrl = imageUrl

        userSearchRepository.save(newUser)
        return userGateway.save(newUser)
    }

    /**
     * @return login if provider manage a login like Twitter or GitHub otherwise email address.
     * Because provider like Google or Facebook didn't provide login or login like "12099388847393"
     */
    private fun getLoginDependingOnProviderId(userProfile: UserProfile, providerId: String): String {
        when (providerId) {
            "twitter" -> return userProfile.username.toLowerCase()
            else -> return userProfile.firstName.toLowerCase() + "_" + userProfile.lastName.toLowerCase()
        }
    }

    private fun createSocialConnection(login: String?, connection: Connection<*>) {
        val connectionRepository = usersConnectionRepository.createConnectionRepository(login)
        connectionRepository.addConnection(connection)
    }

}
