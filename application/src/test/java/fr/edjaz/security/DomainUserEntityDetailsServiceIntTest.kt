package fr.edjaz.security

import fr.edjaz.AppApp
import fr.edjaz.domain.UserEntity
import fr.edjaz.repository.UserRepository

import org.apache.commons.lang3.RandomStringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

import java.util.Locale

import org.assertj.core.api.Assertions.assertThat

/**
 * Test class for DomainUserDetailsService.
 *
 * @see DomainUserDetailsService
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
@Transactional
class DomainUserEntityDetailsServiceIntTest {

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val domainUserDetailsService: UserDetailsService? = null

    private var userOne: UserEntity? = null
    private var userTwo: UserEntity? = null
    private var userThree: UserEntity? = null

    @Before
    fun init() {
        userOne = UserEntity()
        userOne!!.login = USER_ONE_LOGIN
        userOne!!.password = RandomStringUtils.random(60)
        userOne!!.activated = true
        userOne!!.email = USER_ONE_EMAIL
        userOne!!.firstName = "userOne"
        userOne!!.lastName = "doe"
        userOne!!.langKey = "en"
        userRepository!!.save<UserEntity>(userOne)

        userTwo = UserEntity()
        userTwo!!.login = USER_TWO_LOGIN
        userTwo!!.password = RandomStringUtils.random(60)
        userTwo!!.activated = true
        userTwo!!.email = USER_TWO_EMAIL
        userTwo!!.firstName = "userTwo"
        userTwo!!.lastName = "doe"
        userTwo!!.langKey = "en"
        userRepository.save<UserEntity>(userTwo)

        userThree = UserEntity()
        userThree!!.login = USER_THREE_LOGIN
        userThree!!.password = RandomStringUtils.random(60)
        userThree!!.activated = false
        userThree!!.email = USER_THREE_EMAIL
        userThree!!.firstName = "userThree"
        userThree!!.lastName = "doe"
        userThree!!.langKey = "en"
        userRepository.save<UserEntity>(userThree)
    }

    @Test
    @Transactional
    fun assertThatUserCanBeFoundByLogin() {
        val userDetails = domainUserDetailsService!!.loadUserByUsername(USER_ONE_LOGIN)
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(USER_ONE_LOGIN)
    }

    @Test
    @Transactional
    fun assertThatUserCanBeFoundByLoginIgnoreCase() {
        val userDetails = domainUserDetailsService!!.loadUserByUsername(USER_ONE_LOGIN.toUpperCase(Locale.ENGLISH))
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(USER_ONE_LOGIN)
    }

    @Test
    @Transactional
    fun assertThatUserCanBeFoundByEmail() {
        val userDetails = domainUserDetailsService!!.loadUserByUsername(USER_TWO_EMAIL)
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(USER_TWO_LOGIN)
    }

    @Test
    @Transactional
    fun assertThatUserCanBeFoundByEmailIgnoreCase() {
        val userDetails = domainUserDetailsService!!.loadUserByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH))
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(USER_TWO_LOGIN)
    }

    @Test
    @Transactional
    fun assertThatEmailIsPrioritizedOverLogin() {
        val userDetails = domainUserDetailsService!!.loadUserByUsername(USER_ONE_EMAIL.toUpperCase(Locale.ENGLISH))
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(USER_ONE_LOGIN)
    }

    @Test(expected = UserNotActivatedException::class)
    @Transactional
    fun assertThatUserNotActivatedExceptionIsThrownForNotActivatedUsers() {
        domainUserDetailsService!!.loadUserByUsername(USER_THREE_LOGIN)
    }

    companion object {

        private val USER_ONE_LOGIN = "test-user-one"
        private val USER_ONE_EMAIL = "test-user-one@localhost"
        private val USER_TWO_LOGIN = "test-user-two"
        private val USER_TWO_EMAIL = "test-user-two@localhost"
        private val USER_THREE_LOGIN = "test-user-three"
        private val USER_THREE_EMAIL = "test-user-three@localhost"
    }

}
