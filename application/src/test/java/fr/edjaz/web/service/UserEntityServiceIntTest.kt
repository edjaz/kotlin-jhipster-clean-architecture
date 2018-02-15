package fr.edjaz.web.service

import fr.edjaz.AppApp
import fr.edjaz.domain.UserEntity
import fr.edjaz.domain.config.Constants
import fr.edjaz.repository.UserRepository
import fr.edjaz.web.service.user.CompletePasswordReset
import fr.edjaz.web.service.user.GetAllManagedUsers
import fr.edjaz.web.service.user.RemoveNotActivatedUsers
import fr.edjaz.web.service.user.RequestPasswordReset
import fr.edjaz.web.service.user.request.CompletePasswordResetRequest
import fr.edjaz.web.service.util.RandomUtil
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
@Transactional
class UserEntityServiceIntTest {

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val removeNotActivatedUsers: RemoveNotActivatedUsers? = null

    @Autowired
    private val requestPasswordReset: RequestPasswordReset? = null

    @Autowired
    private val getAllManagedUsers: GetAllManagedUsers? = null

    @Autowired
    private val completePasswordReset: CompletePasswordReset? = null



    private var user: UserEntity? = null

    @Before
    fun init() {
        user = UserEntity()
        user!!.login = "johndoe"
        user!!.password = RandomStringUtils.random(60)
        user!!.activated = true
        user!!.email = "johndoe@localhost"
        user!!.firstName = "john"
        user!!.lastName = "doe"
        user!!.imageUrl = "http://placehold.it/50x50"
        user!!.langKey = "en"
    }

    @Test
    @Transactional
    fun assertThatUserMustExistToResetPassword() {
        userRepository!!.saveAndFlush<UserEntity>(user)
        var maybeUser = requestPasswordReset!!.execute("invalid.login@localhost")
        assertThat(maybeUser).isNotPresent

        maybeUser = requestPasswordReset.execute(user!!.email!!)
        assertThat(maybeUser).isPresent
        assertThat(maybeUser.orElse(null).email).isEqualTo(user!!.email)
        assertThat(maybeUser.orElse(null).resetDate).isNotNull()
        assertThat(maybeUser.orElse(null).resetKey).isNotNull()
    }

    @Test
    @Transactional
    fun assertThatOnlyActivatedUserCanRequestPasswordReset() {
        user!!.activated = false
        userRepository!!.saveAndFlush<UserEntity>(user)

        val maybeUser = requestPasswordReset!!.execute(user!!.login!!)
        assertThat(maybeUser).isNotPresent
        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun assertThatResetKeyMustNotBeOlderThan24Hours() {
        val daysAgo = Instant.now().minus(25, ChronoUnit.HOURS)
        val resetKey = RandomUtil.generateResetKey()
        user!!.activated = true
        user!!.resetDate = daysAgo
        user!!.resetKey = resetKey
        userRepository!!.saveAndFlush<UserEntity>(user)

        val maybeUser = completePasswordReset!!.execute(CompletePasswordResetRequest("johndoe2", user!!.resetKey!!))
        assertThat(maybeUser).isNotPresent
        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun assertThatResetKeyMustBeValid() {
        val daysAgo = Instant.now().minus(25, ChronoUnit.HOURS)
        user!!.activated = true
        user!!.resetDate = daysAgo
        user!!.resetKey = "1234"
        userRepository!!.saveAndFlush<UserEntity>(user)

        val maybeUser = completePasswordReset!!.execute(CompletePasswordResetRequest("johndoe2", user!!.resetKey!!))
        assertThat(maybeUser).isNotPresent
        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun assertThatUserCanResetPassword() {
        val oldPassword = user!!.password
        val daysAgo = Instant.now().minus(2, ChronoUnit.HOURS)
        val resetKey = RandomUtil.generateResetKey()
        user!!.activated = true
        user!!.resetDate = daysAgo
        user!!.resetKey = resetKey
        userRepository!!.saveAndFlush<UserEntity>(user)

        val maybeUser = completePasswordReset!!.execute(CompletePasswordResetRequest("johndoe2", user!!.resetKey!!))
        assertThat(maybeUser).isPresent
        assertThat(maybeUser.orElse(null).resetDate).isNull()
        assertThat(maybeUser.orElse(null).resetKey).isNull()
        assertThat(maybeUser.orElse(null).password).isNotEqualTo(oldPassword)

        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun testFindNotActivatedUsersByCreationDateBefore() {
        val now = Instant.now()
        user!!.activated = false
        val dbUser = userRepository!!.saveAndFlush<UserEntity>(user)
        dbUser.createdDate = now.minus(4, ChronoUnit.DAYS)
        userRepository.saveAndFlush<UserEntity>(user)
        var users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS))
        assertThat(users).isNotEmpty
        removeNotActivatedUsers!!.execute()
        users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS))
        assertThat(users).isEmpty()
    }

    @Test
    @Transactional
    fun assertThatAnonymousUserIsNotGet() {
        user!!.login = Constants.ANONYMOUS_USER
        if (!userRepository!!.findOneByLogin(Constants.ANONYMOUS_USER).isPresent) {
            userRepository.saveAndFlush<UserEntity>(user)
        }
        val pageable = PageRequest(0, userRepository.count().toInt())
        val allManagedUsers = getAllManagedUsers!!.execute(pageable)
        assertThat(allManagedUsers.content.stream()
            .noneMatch { user -> Constants.ANONYMOUS_USER == user.login })
            .isTrue()
    }

    @Test
    @Transactional
    fun testRemoveNotActivatedUsers() {
        user!!.activated = false
        userRepository!!.saveAndFlush<UserEntity>(user)
        // Let the audit first set the creation date but then update it
        user!!.createdDate = Instant.now().minus(30, ChronoUnit.DAYS)
        userRepository.saveAndFlush<UserEntity>(user)

        assertThat(userRepository.findOneByLogin("johndoe")).isPresent
        removeNotActivatedUsers!!.execute()
        assertThat(userRepository.findOneByLogin("johndoe")).isNotPresent
    }

}
