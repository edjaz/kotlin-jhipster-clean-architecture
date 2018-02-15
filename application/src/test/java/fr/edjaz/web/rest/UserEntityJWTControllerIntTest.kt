package fr.edjaz.web.rest

import fr.edjaz.AppApp
import fr.edjaz.domain.UserEntity
import fr.edjaz.repository.UserRepository
import fr.edjaz.web.rest.vm.LoginVM
import fr.edjaz.web.rest.errors.ExceptionTranslator
import fr.edjaz.web.rest.util.TestUtil
import fr.edjaz.web.security.jwt.TokenProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not

/**
 * Test class for the UserJWTController REST controller.
 *
 * @see UserJWTController
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
class UserEntityJWTControllerIntTest {

    @Autowired
    private val tokenProvider: TokenProvider? = null

    @Autowired
    private val authenticationManager: AuthenticationManager? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @Autowired
    private val exceptionTranslator: ExceptionTranslator? = null

    private var mockMvc: MockMvc? = null

    @Before
    fun setup() {
        val userJWTController = UserJWTController(tokenProvider!!, authenticationManager!!)
        this.mockMvc = MockMvcBuilders.standaloneSetup(userJWTController)
                .setControllerAdvice(exceptionTranslator!!)
                .build()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorize() {
        val user = UserEntity()
        user.login = "user-jwt-controller"
        user.email = "user-jwt-controller@example.com"
        user.activated = true
        user.password = passwordEncoder!!.encode("test")

        userRepository!!.saveAndFlush(user)

        val login = LoginVM()
        login.username = "user-jwt-controller"
        login.password = "test"
        mockMvc!!.perform(post("/api/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(login)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id_token").isString)
                .andExpect(jsonPath("$.id_token").isNotEmpty)
                .andExpect(header().string("Authorization", not(nullValue())))
                .andExpect(header().string("Authorization", not(isEmptyString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorizeWithRememberMe() {
        val user = UserEntity()
        user.login = "user-jwt-controller-remember-me"
        user.email = "user-jwt-controller-remember-me@example.com"
        user.activated = true
        user.password = passwordEncoder!!.encode("test")

        userRepository!!.saveAndFlush(user)

        val login = LoginVM()
        login.username = "user-jwt-controller-remember-me"
        login.password = "test"
        login.isRememberMe = true
        mockMvc!!.perform(post("/api/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(login)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id_token").isString)
                .andExpect(jsonPath("$.id_token").isNotEmpty)
                .andExpect(header().string("Authorization", not(nullValue())))
                .andExpect(header().string("Authorization", not(isEmptyString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorizeFails() {
        val login = LoginVM()
        login.username = "wrong-user"
        login.password = "wrong password"
        mockMvc!!.perform(post("/api/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(login)))
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.id_token").doesNotExist())
                .andExpect(header().doesNotExist("Authorization"))
    }
}
