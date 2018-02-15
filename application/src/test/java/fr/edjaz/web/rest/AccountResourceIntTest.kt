package fr.edjaz.web.rest

import fr.edjaz.AppApp
import fr.edjaz.domain.config.Constants
import fr.edjaz.domain.gateway.AuthorityGateway
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.model.User
import fr.edjaz.web.dto.UserDTO
import fr.edjaz.web.service.mail.SendActivationEmail
import fr.edjaz.web.service.mail.SendPasswordResetMail
import fr.edjaz.web.service.mail.request.MailUser
import fr.edjaz.web.service.mail.request.SendEmailUserRequest
import fr.edjaz.web.service.security.AuthoritiesConstants
import fr.edjaz.web.service.user.*
import fr.edjaz.web.service.user.response.AuthorityResponse
import fr.edjaz.web.service.user.response.UserModelResponse
import fr.edjaz.web.rest.errors.ExceptionTranslator
import fr.edjaz.web.rest.util.TestUtil
import fr.edjaz.web.rest.vm.KeyAndPasswordVM
import fr.edjaz.web.rest.vm.ManagedUserVM
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
class AccountResourceIntTest {

    @Autowired
    private val userGateway: UserGateway? = null

    @Autowired
    private val authorityGateway: AuthorityGateway? = null

    @Autowired
    private val getUserWithAuthoritiesByLogin: GetUserWithAuthoritiesByLogin? = null

    @Autowired
    private val activateRegistration: ActivateRegistration? = null


    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @Autowired
    private val httpMessageConverters: Array<HttpMessageConverter<*>>? = null

    @Autowired
    private val exceptionTranslator: ExceptionTranslator? = null

    @Autowired
    private val requestPasswordReset: RequestPasswordReset? = null

    @Autowired
    private val completePasswordReset: CompletePasswordReset? = null

    @Autowired
    private val updateUser: UpdateUser? = null

    @Autowired
    private val registerUser: RegisterUser? = null

    @Autowired
    private val changePassword: ChangePassword? = null


    @Autowired
    private val sendPasswordResetMail: SendPasswordResetMail? = null

    @Autowired
    private val getUserWithAuthorities: GetUserWithAuthorities? = null


    @Mock
    private val changePasswordMock: ChangePassword? = null


    @Mock
    private val getUserWithAuthoritiesMock: GetUserWithAuthorities? = null



    @Mock
    private val activateRegistrationMock: ActivateRegistration? = null


    @Mock
    private val requestPasswordResetMock: RequestPasswordReset? = null

    @Mock
    private val completePasswordResetMock: CompletePasswordReset? = null

    @Mock
    private val registerUserMock: RegisterUser? = null

    @Mock
    private val updateUserMock: UpdateUser? = null

    @Mock
    private val sendPasswordResetMailMock: SendPasswordResetMail? = null


    @Mock
    private val sendActivationEmailMock: SendActivationEmail? = null

    @Autowired
    private val sendActivationEmail: SendActivationEmail? = null


    private var restMvc: MockMvc? = null

    private var restUserMockMvc: MockMvc? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        doNothing().`when`(sendActivationEmailMock)!!.execute(SendEmailUserRequest(MailUser()))
        val accountResource =
            AccountResource(getUserWithAuthorities!!, activateRegistration!!, completePasswordReset!!, requestPasswordReset!!, registerUser!!, changePassword!!, updateUser!!, sendActivationEmail!!, sendPasswordResetMail!!);

        val accountUserMockResource =
            AccountResource( getUserWithAuthoritiesMock!!, activateRegistrationMock!!, completePasswordResetMock!!, requestPasswordResetMock!!, registerUserMock!!, changePasswordMock!!, updateUserMock!!, sendActivationEmailMock!!, sendPasswordResetMailMock!!);
        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource)
            .setMessageConverters(*httpMessageConverters!!)
            .setControllerAdvice(exceptionTranslator)
            .build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
            .setControllerAdvice(exceptionTranslator)
            .build();

    }

    @Test
    @Throws(Exception::class)
    fun testNonAuthenticatedUser() {
        restUserMockMvc!!.perform(get("/api/authenticate")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().string(""))
    }

    @Test
    @Throws(Exception::class)
    fun testAuthenticatedUser() {
        restUserMockMvc!!.perform(get("/api/authenticate")
            .with { request ->
                request.remoteUser = "test"
                request
            }
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().string("test"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetExistingAccount() {
        val authorities = HashSet<AuthorityResponse>()
        val authority = AuthorityResponse()
        authority.name = AuthoritiesConstants.ADMIN
        authorities.add(authority)

        val user = UserModelResponse(
            login = "test",
            firstName = "john",
            lastName = "doe",
            email = "john.doe@jhipster.com",
            imageUrl = "http://placehold.it/50x50",
            langKey = "en",
            authorities = authorities
        )

        `when`(getUserWithAuthoritiesMock!!.execute()).thenReturn(Optional.of(user))

        restUserMockMvc!!.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("test"))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.imageUrl").value("http://placehold.it/50x50"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
    }

    @Test
    @Throws(Exception::class)
    fun testGetUnknownAccount() {
        `when`(getUserWithAuthoritiesMock!!.execute()).thenReturn(Optional.empty())

        restUserMockMvc!!.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterValid() {
        val validUser = ManagedUserVM()
        validUser.login = "joe"
        validUser.password = "password"
        validUser.firstName = "Joe"
        validUser.lastName = "Shmoe"
        validUser.email = "joe@example.com"
        validUser.isActivated = true
        validUser.imageUrl = "http://placehold.it/50x50"
        validUser.langKey = Constants.DEFAULT_LANGUAGE
        validUser.authorities = setOf(AuthoritiesConstants.USER)
        assertThat(userGateway!!.findOneByLogin("joe").isPresent).isFalse()

        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated)

        assertThat(userGateway.findOneByLogin("joe").isPresent).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidLogin() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "funky-log!n"// <-- invalid
        invalidUser.password = "password"
        invalidUser.firstName = "Funky"
        invalidUser.lastName = "One"
        invalidUser.email = "funky@example.com"
        invalidUser.isActivated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest)

        val user = userGateway!!.findOneByEmailIgnoreCase("funky@example.com")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidEmail() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "bob"
        invalidUser.password = "password"
        invalidUser.firstName = "Bob"
        invalidUser.lastName = "Green"
        invalidUser.email = "invalid"// <-- invalid
        invalidUser.isActivated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest)

        val user = userGateway!!.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidPassword() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "bob"
        invalidUser.password = "123"// password with only 3 digits
        invalidUser.firstName = "Bob"
        invalidUser.lastName = "Green"
        invalidUser.email = "bob@example.com"
        invalidUser.isActivated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest)

        val user = userGateway!!.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterNullPassword() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "bob"
        invalidUser.password = null// invalid null password
        invalidUser.firstName = "Bob"
        invalidUser.lastName = "Green"
        invalidUser.email = "bob@example.com"
        invalidUser.isActivated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest)

        val user = userGateway!!.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterDuplicateLogin() {
        // Good
        val validUser = ManagedUserVM()
        validUser.login = "alice"
        validUser.password = "password"
        validUser.firstName = "Alice"
        validUser.lastName = "Something"
        validUser.email = "alice@example.com"
        validUser.isActivated = true
        validUser.imageUrl = "http://placehold.it/50x50"
        validUser.langKey = Constants.DEFAULT_LANGUAGE
        validUser.authorities = setOf(AuthoritiesConstants.USER)

        // Duplicate login, different email
        val duplicatedUser = ManagedUserVM()
        duplicatedUser.login = validUser.login
        duplicatedUser.password = validUser.password
        duplicatedUser.firstName = validUser.firstName
        duplicatedUser.lastName = validUser.lastName
        duplicatedUser.email = "alicejr@example.com"
        duplicatedUser.isActivated = validUser.isActivated
        duplicatedUser.imageUrl = validUser.imageUrl
        duplicatedUser.langKey = validUser.langKey
        duplicatedUser.createdBy = validUser.createdBy
        duplicatedUser.createdDate = validUser.createdDate
        duplicatedUser.lastModifiedBy = validUser.lastModifiedBy
        duplicatedUser.lastModifiedDate = validUser.lastModifiedDate
        duplicatedUser.authorities = HashSet(validUser.authorities!!)

        // Good user
        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated)

        // Duplicate login
        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError)

        val userDup = userGateway!!.findOneByEmailIgnoreCase("alicejr@example.com")
        assertThat(userDup.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterDuplicateEmail() {
        // Good
        val validUser = ManagedUserVM()
        validUser.login = "john"
        validUser.password = "password"
        validUser.firstName = "John"
        validUser.lastName = "Doe"
        validUser.email = "john@example.com"
        validUser.isActivated = true
        validUser.imageUrl = "http://placehold.it/50x50"
        validUser.langKey = Constants.DEFAULT_LANGUAGE
        validUser.authorities = setOf(AuthoritiesConstants.USER)

        // Duplicate email, different login
        val duplicatedUser = ManagedUserVM()
        duplicatedUser.login = "johnjr"
        duplicatedUser.password = validUser.password
        duplicatedUser.firstName = validUser.firstName
        duplicatedUser.lastName = validUser.lastName
        duplicatedUser.email = validUser.email
        duplicatedUser.isActivated = validUser.isActivated
        duplicatedUser.imageUrl = validUser.imageUrl
        duplicatedUser.langKey = validUser.langKey
        duplicatedUser.createdBy = validUser.createdBy
        duplicatedUser.createdDate = validUser.createdDate
        duplicatedUser.lastModifiedBy = validUser.lastModifiedBy
        duplicatedUser.lastModifiedDate = validUser.lastModifiedDate
        duplicatedUser.authorities = HashSet(validUser.authorities!!)

        // Good user
        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated)

        // Duplicate email
        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError)

        // Duplicate email - with uppercase email address
        val userWithUpperCaseEmail = ManagedUserVM()
        userWithUpperCaseEmail.id = validUser.id
        userWithUpperCaseEmail.login = "johnjr"
        userWithUpperCaseEmail.password = validUser.password
        userWithUpperCaseEmail.firstName = validUser.firstName
        userWithUpperCaseEmail.lastName = validUser.lastName
        userWithUpperCaseEmail.email = validUser.email!!.toUpperCase()
        userWithUpperCaseEmail.isActivated = validUser.isActivated
        userWithUpperCaseEmail.imageUrl = validUser.imageUrl
        userWithUpperCaseEmail.langKey = validUser.langKey
        userWithUpperCaseEmail.createdBy = validUser.createdBy
        userWithUpperCaseEmail.createdDate = validUser.createdDate
        userWithUpperCaseEmail.lastModifiedBy = validUser.lastModifiedBy
        userWithUpperCaseEmail.lastModifiedDate = validUser.lastModifiedDate
        userWithUpperCaseEmail.authorities = HashSet(validUser.authorities!!)

        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithUpperCaseEmail)))
            .andExpect(status().is4xxClientError)

        val userDup = userGateway!!.findOneByLogin("johnjr")
        assertThat(userDup.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterAdminIsIgnored() {
        val validUser = ManagedUserVM()
        validUser.login = "badguy"
        validUser.password = "password"
        validUser.firstName = "Bad"
        validUser.lastName = "Guy"
        validUser.email = "badguy@example.com"
        validUser.isActivated = true
        validUser.imageUrl = "http://placehold.it/50x50"
        validUser.langKey = Constants.DEFAULT_LANGUAGE
        validUser.authorities = setOf(AuthoritiesConstants.USER)

        restMvc!!.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated)

        val userDup = userGateway!!.findOneByLogin("badguy")
        assertThat(userDup.isPresent).isTrue()
        assertThat(userDup.get().authorities).hasSize(1)
            .containsExactly(authorityGateway!!.findOne(AuthoritiesConstants.USER))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testActivateAccount() {
        val activationKey = "some activation key"
        var user = User()
        user.login = "activate-account"
        user.email = "activate-account@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = false
        user.activationKey = activationKey

        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(get("/api/activate?key={activationKey}", activationKey))
            .andExpect(status().isOk)

        user = userGateway.findOneByLogin(user.login!!).orElse(null)
        assertThat(user.activated).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testActivateAccountWithWrongKey() {
        restMvc!!.perform(get("/api/activate?key=wrongActivationKey"))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    @Throws(Exception::class)
    fun testSaveAccount() {
        val user = User()
        user.login = "save-account"
        user.email = "save-account@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userGateway!!.saveAndFlush(user)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "save-account@example.com"
        userDTO.isActivated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc!!.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isOk)

        val (_, _, password, firstName, lastName, email, activated, langKey, imageUrl, _, _, _, authorities) = userGateway.findOneByLogin(user.login!!).orElse(null)
        assertThat(firstName).isEqualTo(userDTO.firstName)
        assertThat(lastName).isEqualTo(userDTO.lastName)
        assertThat(email).isEqualTo(userDTO.email)
        assertThat(langKey).isEqualTo(userDTO.langKey)
        assertThat(password).isEqualTo(user.password)
        assertThat(imageUrl).isEqualTo(userDTO.imageUrl)
        assertThat(activated).isEqualTo(true)
        assertThat(authorities).isEmpty()
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    @Throws(Exception::class)
    fun testSaveInvalidEmail() {
        val user = User()
        user.login = "save-invalid-email"
        user.email = "save-invalid-email@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userGateway!!.saveAndFlush(user)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "invalid email"
        userDTO.isActivated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc!!.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isBadRequest)

        assertThat(userGateway.findOneByEmailIgnoreCase("invalid email")).isNotPresent
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    @Throws(Exception::class)
    fun testSaveExistingEmail() {
        val user = User()
        user.login = "save-existing-email"
        user.email = "save-existing-email@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userGateway!!.saveAndFlush(user)

        val anotherUser = User()
        anotherUser.login = "save-existing-email2"
        anotherUser.email = "save-existing-email2@example.com"
        anotherUser.password = RandomStringUtils.random(60)
        anotherUser.activated = true

        userGateway.saveAndFlush(anotherUser)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "save-existing-email2@example.com"
        userDTO.isActivated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc!!.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isBadRequest)

        val (_, _, _, _, _, email) = userGateway.findOneByLogin("save-existing-email").orElse(null)
        assertThat(email).isEqualTo("save-existing-email@example.com")
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    @Throws(Exception::class)
    fun testSaveExistingEmailAndLogin() {
        val user = User()
        user.login = "save-existing-email-and-login"
        user.email = "save-existing-email-and-login@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userGateway!!.saveAndFlush(user)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "save-existing-email-and-login@example.com"
        userDTO.isActivated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc!!.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isOk)

        val (_, _, _, _, _, email) = userGateway.findOneByLogin("save-existing-email-and-login").orElse(null)
        assertThat(email).isEqualTo("save-existing-email-and-login@example.com")
    }

    @Test
    @Transactional
    @WithMockUser("change-password")
    @Throws(Exception::class)
    fun testChangePassword() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "change-password"
        user.email = "change-password@example.com"
        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(post("/api/account/change-password").content("new password"))
            .andExpect(status().isOk)

        val (_, _, password) = userGateway.findOneByLogin("change-password").orElse(null)
        assertThat(passwordEncoder!!.matches("new password", password)).isTrue()
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    @Throws(Exception::class)
    fun testChangePasswordTooSmall() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "change-password-too-small"
        user.email = "change-password-too-small@example.com"
        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(post("/api/account/change-password").content("new"))
            .andExpect(status().isBadRequest)

        val (_, _, password) = userGateway.findOneByLogin("change-password-too-small").orElse(null)
        assertThat(password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    @Throws(Exception::class)
    fun testChangePasswordTooLong() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "change-password-too-long"
        user.email = "change-password-too-long@example.com"
        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(post("/api/account/change-password").content(RandomStringUtils.random(101)))
            .andExpect(status().isBadRequest)

        val (_, _, password) = userGateway.findOneByLogin("change-password-too-long").orElse(null)
        assertThat(password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    @Throws(Exception::class)
    fun testChangePasswordEmpty() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "change-password-empty"
        user.email = "change-password-empty@example.com"
        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(post("/api/account/change-password").content(RandomStringUtils.random(0)))
            .andExpect(status().isBadRequest)

        val (_, _, password) = userGateway.findOneByLogin("change-password-empty").orElse(null)
        assertThat(password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRequestPasswordReset() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.activated = true
        user.login = "password-reset"
        user.email = "password-reset@example.com"
        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(post("/api/account/reset-password/init")
            .content("password-reset@example.com"))
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRequestPasswordResetUpperCaseEmail() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.activated = true
        user.login = "password-reset"
        user.email = "password-reset@example.com"
        userGateway!!.saveAndFlush(user)

        restMvc!!.perform(post("/api/account/reset-password/init")
            .content("password-reset@EXAMPLE.COM"))
            .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testRequestPasswordResetWrongEmail() {
        restMvc!!.perform(
            post("/api/account/reset-password/init")
                .content("password-reset-wrong-email@example.com"))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordReset() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "finish-password-reset"
        user.email = "finish-password-reset@example.com"
        user.resetDate = Instant.now().plusSeconds(60)
        user.resetKey = "reset key"
        userGateway!!.saveAndFlush(user)

        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = user.resetKey
        keyAndPassword.newPassword = "new password"

        restMvc!!.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isOk)

        val (_, _, password) = userGateway.findOneByLogin(user.login!!).orElse(null)
        assertThat(passwordEncoder!!.matches(keyAndPassword.newPassword, password)).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordResetTooSmall() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "finish-password-reset-too-small"
        user.email = "finish-password-reset-too-small@example.com"
        user.resetDate = Instant.now().plusSeconds(60)
        user.resetKey = "reset key too small"
        userGateway!!.saveAndFlush(user)

        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = user.resetKey
        keyAndPassword.newPassword = "foo"

        restMvc!!.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isBadRequest)

        val (_, _, password) = userGateway.findOneByLogin(user.login!!).orElse(null)
        assertThat(passwordEncoder!!.matches(keyAndPassword.newPassword, password)).isFalse()
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordResetWrongKey() {
        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = "wrong reset key"
        keyAndPassword.newPassword = "new password"

        restMvc!!.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isInternalServerError)
    }
}
