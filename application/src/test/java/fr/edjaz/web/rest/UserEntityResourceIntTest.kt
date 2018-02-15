package fr.edjaz.web.rest

import fr.edjaz.AppApp
import fr.edjaz.domain.AuthorityEntity
import fr.edjaz.domain.UserEntity
import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.domain.model.User
import fr.edjaz.web.mapper.UserMapper
import fr.edjaz.web.rest.errors.ExceptionTranslator
import fr.edjaz.web.rest.util.TestUtil
import fr.edjaz.web.rest.vm.ManagedUserVM
import fr.edjaz.web.service.mail.SendCreationEmail
import fr.edjaz.web.service.security.AuthoritiesConstants
import fr.edjaz.web.service.user.*
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
@WithMockUser
class UserEntityResourceIntTest {

    @Autowired
    private val userGateway: UserGateway? = null

    @Autowired
    private val getAuthorities: GetAuthorities? = null

    @Autowired
    private val userSearchRepository: UserSearchGateway? = null

    @Autowired
    private val deleteUser: DeleteUser? = null

    @Autowired
    private val userMapper: UserMapper? = null

    @Autowired
    private val createUser: CreateUser? = null

    @Autowired
    private val jacksonMessageConverter: MappingJackson2HttpMessageConverter? = null

    @Autowired
    private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver? = null

    @Autowired
    private val exceptionTranslator: ExceptionTranslator? = null

    @Autowired
    private val updateUser: UpdateUser? = null


    @Autowired
    private val userSearch: UserSearch? = null


    @Autowired
    private val getAllManagedUsers: GetAllManagedUsers? = null

    @Autowired
    private val getUserWithAuthoritiesByLogin: GetUserWithAuthoritiesByLogin? = null

    @Autowired
    private val em: EntityManager? = null

    @Autowired
    private val cacheManager: CacheManager? = null

    @Autowired
    private val sendCreationEmail: SendCreationEmail? = null

    private var restUserMockMvc: MockMvc? = null

    private var user: User? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        cacheManager!!.getCache(UserGateway.USERS_BY_LOGIN_CACHE).clear()
        cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).clear()
        val userResource = UserResource(getAuthorities!!, createUser!!, updateUser!!, deleteUser!!, sendCreationEmail!!, getAllManagedUsers!!, getUserWithAuthoritiesByLogin!!, userSearch!!)
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
            .setCustomArgumentResolvers(pageableArgumentResolver!!)
            .setControllerAdvice(exceptionTranslator!!)
            .setMessageConverters(jacksonMessageConverter!!)
            .build()
    }

    @Before
    fun initTest() {
        user = createEntity(em)
        user!!.login = DEFAULT_LOGIN
        user!!.email = DEFAULT_EMAIL
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createUser() {
        val databaseSizeBeforeCreate = userGateway!!.findAll().size

        // Create the User
        val managedUserVM = ManagedUserVM()
        managedUserVM.login = DEFAULT_LOGIN
        managedUserVM.password = DEFAULT_PASSWORD
        managedUserVM.firstName = DEFAULT_FIRSTNAME
        managedUserVM.lastName = DEFAULT_LASTNAME
        managedUserVM.email = DEFAULT_EMAIL
        managedUserVM.isActivated = true
        managedUserVM.imageUrl = DEFAULT_IMAGEURL
        managedUserVM.langKey = DEFAULT_LANGKEY
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isCreated)

        // Validate the User in the database
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1)
        val testUser = userList[userList.size - 1]
        assertThat(testUser.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(testUser.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(testUser.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(testUser.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testUser.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(testUser.langKey).isEqualTo(DEFAULT_LANGKEY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createUserWithExistingId() {
        val databaseSizeBeforeCreate = userGateway!!.findAll().size

        val managedUserVM = ManagedUserVM()
        managedUserVM.id = 1L
        managedUserVM.login = DEFAULT_LOGIN
        managedUserVM.password = DEFAULT_PASSWORD
        managedUserVM.firstName = DEFAULT_FIRSTNAME
        managedUserVM.lastName = DEFAULT_LASTNAME
        managedUserVM.email = DEFAULT_EMAIL
        managedUserVM.isActivated = true
        managedUserVM.imageUrl = DEFAULT_IMAGEURL
        managedUserVM.langKey = DEFAULT_LANGKEY
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc!!.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest)

        // Validate the User in the database
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createUserWithExistingLogin() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!)
        userSearchRepository!!.save(user!!)
        val databaseSizeBeforeCreate = userGateway.findAll().size

        val managedUserVM = ManagedUserVM()
        managedUserVM.login = DEFAULT_LOGIN// this login should already be used
        managedUserVM.password = DEFAULT_PASSWORD
        managedUserVM.firstName = DEFAULT_FIRSTNAME
        managedUserVM.lastName = DEFAULT_LASTNAME
        managedUserVM.email = "anothermail@localhost"
        managedUserVM.isActivated = true
        managedUserVM.imageUrl = DEFAULT_IMAGEURL
        managedUserVM.langKey = DEFAULT_LANGKEY
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        // Create the User
        restUserMockMvc!!.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest)

        // Validate the User in the database
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createUserWithExistingEmail() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!)
        userSearchRepository!!.save(user!!)

        val databaseSizeBeforeCreate = userGateway.findAll().size

        val managedUserVM = ManagedUserVM()
        managedUserVM.login = "anotherlogin"
        managedUserVM.password = DEFAULT_PASSWORD
        managedUserVM.firstName = DEFAULT_FIRSTNAME
        managedUserVM.lastName = DEFAULT_LASTNAME
        managedUserVM.email = DEFAULT_EMAIL// this email should already be used
        managedUserVM.isActivated = true
        managedUserVM.imageUrl = DEFAULT_IMAGEURL
        managedUserVM.langKey = DEFAULT_LANGKEY
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        // Create the User
        restUserMockMvc!!.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest)

        // Validate the User in the database
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllUsers() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!)
        userSearchRepository!!.save(user!!)

        // Get all the users
        restUserMockMvc!!.perform(get("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGEURL)))
            .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    @WithMockUser(username = DEFAULT_LOGIN, roles = ["USER", "ADMIN"])
    fun getUser() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!)
        userSearchRepository!!.save(user!!)

        assertThat(cacheManager!!.getCache(UserGateway.USERS_BY_LOGIN_CACHE).get(user!!.login)).isNull()

        // Get the user
        restUserMockMvc!!.perform(get("/api/users/{login}", user!!.login))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value(user!!.login))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGEURL))
            .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY))

        assertThat(cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).get(user!!.login)).isNotNull()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingUser() {
        restUserMockMvc!!.perform(get("/api/users/unknown"))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateUser() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!)
        userSearchRepository!!.save(user!!)

        val databaseSizeBeforeUpdate = userGateway.findAll().size

        // Update the user
        val updatedUser = userGateway.findOne(user!!.id)

        val managedUserVM = ManagedUserVM()
        managedUserVM.id = updatedUser.id
        managedUserVM.login = updatedUser.login
        managedUserVM.password = UPDATED_PASSWORD
        managedUserVM.firstName = UPDATED_FIRSTNAME
        managedUserVM.lastName = UPDATED_LASTNAME
        managedUserVM.email = UPDATED_EMAIL
        managedUserVM.isActivated = updatedUser.activated
        managedUserVM.imageUrl = UPDATED_IMAGEURL
        managedUserVM.langKey = UPDATED_LANGKEY
        managedUserVM.createdBy = updatedUser.createdBy
        managedUserVM.createdDate = updatedUser.createdDate
        managedUserVM.lastModifiedBy = updatedUser.lastModifiedBy
        managedUserVM.lastModifiedDate = updatedUser.lastModifiedDate
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk)

        // Validate the User in the database
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeUpdate)
        val (_, _, _, firstName, lastName, email, _, langKey, imageUrl) = userList[userList.size - 1]
        assertThat(firstName).isEqualTo(UPDATED_FIRSTNAME)
        assertThat(lastName).isEqualTo(UPDATED_LASTNAME)
        assertThat(email).isEqualTo(UPDATED_EMAIL)
        assertThat(imageUrl).isEqualTo(UPDATED_IMAGEURL)
        assertThat(langKey).isEqualTo(UPDATED_LANGKEY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateUserLogin() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!);
        userSearchRepository!!.save(user!!)

        val databaseSizeBeforeUpdate = userGateway.findAll().size

        // Update the user
        val updatedUser = userGateway.findOne(user!!.id)

        val managedUserVM = ManagedUserVM()
        managedUserVM.id = updatedUser.id
        managedUserVM.login = UPDATED_LOGIN
        managedUserVM.password = UPDATED_PASSWORD
        managedUserVM.firstName = UPDATED_FIRSTNAME
        managedUserVM.lastName = UPDATED_LASTNAME
        managedUserVM.email = UPDATED_EMAIL
        managedUserVM.isActivated = updatedUser.activated
        managedUserVM.imageUrl = UPDATED_IMAGEURL
        managedUserVM.langKey = UPDATED_LANGKEY
        managedUserVM.createdBy = updatedUser.createdBy
        managedUserVM.createdDate = updatedUser.createdDate
        managedUserVM.lastModifiedBy = updatedUser.lastModifiedBy
        managedUserVM.lastModifiedDate = updatedUser.lastModifiedDate
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk)

        // Validate the User in the database
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeUpdate)
        val testUser = userList[userList.size - 1]
        assertThat(testUser.login).isEqualTo(UPDATED_LOGIN)
        assertThat(testUser.firstName).isEqualTo(UPDATED_FIRSTNAME)
        assertThat(testUser.lastName).isEqualTo(UPDATED_LASTNAME)
        assertThat(testUser.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testUser.imageUrl).isEqualTo(UPDATED_IMAGEURL)
        assertThat(testUser.langKey).isEqualTo(UPDATED_LANGKEY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateUserExistingEmail() {
        // Initialize the database with 2 users
        user = userGateway!!.saveAndFlush(user!!);
        userSearchRepository!!.save(user!!);

        val anotherUser = User()
        anotherUser.login = "jhipster"
        anotherUser.password = RandomStringUtils.random(60)
        anotherUser.activated = true
        anotherUser.email = "jhipster@localhost"
        anotherUser.firstName = "java"
        anotherUser.lastName = "hipster"
        anotherUser.imageUrl = ""
        anotherUser.langKey = "en"
        userGateway.saveAndFlush(anotherUser)
        userSearchRepository.save(anotherUser)

        // Update the user
        val updatedUser = userGateway.findOne(user!!.id)

        val managedUserVM = ManagedUserVM()
        managedUserVM.id = updatedUser.id
        managedUserVM.login = updatedUser.login
        managedUserVM.password = updatedUser.password
        managedUserVM.firstName = updatedUser.firstName
        managedUserVM.lastName = updatedUser.lastName
        managedUserVM.email = "jhipster@localhost"// this email should already be used by anotherUser
        managedUserVM.isActivated = updatedUser.activated
        managedUserVM.imageUrl = updatedUser.imageUrl
        managedUserVM.langKey = updatedUser.langKey
        managedUserVM.createdBy = updatedUser.createdBy
        managedUserVM.createdDate = updatedUser.createdDate
        managedUserVM.lastModifiedBy = updatedUser.lastModifiedBy
        managedUserVM.lastModifiedDate = updatedUser.lastModifiedDate
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateUserExistingLogin() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!);
        userSearchRepository!!.save(user!!)

        val anotherUser = User()
        anotherUser.login = "jhipster"
        anotherUser.password = RandomStringUtils.random(60)
        anotherUser.activated = true
        anotherUser.email = "jhipster@localhost"
        anotherUser.firstName = "java"
        anotherUser.lastName = "hipster"
        anotherUser.imageUrl = ""
        anotherUser.langKey = "en"
        userGateway.saveAndFlush(anotherUser)
        userSearchRepository.save(anotherUser)

        // Update the user
        val updatedUser = userGateway.findOne(user!!.id)

        val managedUserVM = ManagedUserVM()
        managedUserVM.id = updatedUser.id
        managedUserVM.login = "jhipster"// this login should already be used by anotherUser
        managedUserVM.password = updatedUser.password
        managedUserVM.firstName = updatedUser.firstName
        managedUserVM.lastName = updatedUser.lastName
        managedUserVM.email = updatedUser.email
        managedUserVM.isActivated = updatedUser.activated
        managedUserVM.imageUrl = updatedUser.imageUrl
        managedUserVM.langKey = updatedUser.langKey
        managedUserVM.createdBy = updatedUser.createdBy
        managedUserVM.createdDate = updatedUser.createdDate
        managedUserVM.lastModifiedBy = updatedUser.lastModifiedBy
        managedUserVM.lastModifiedDate = updatedUser.lastModifiedDate
        managedUserVM.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc!!.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteUser() {
        // Initialize the database
        user = userGateway!!.saveAndFlush(user!!);
        userSearchRepository!!.save(user!!)

        val databaseSizeBeforeDelete = userGateway.findAll().size

        // Delete the user
        restUserMockMvc!!.perform(delete("/api/users/{login}", user!!.login)
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)

        assertThat(cacheManager!!.getCache(UserGateway.USERS_BY_LOGIN_CACHE).get(user!!.login)).isNull()

        // Validate the database is empty
        val userList = userGateway.findAll()
        assertThat(userList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorities() {
        restUserMockMvc!!.perform(get("/api/users/authorities")
            .accept(TestUtil.APPLICATION_JSON_UTF8)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").value(containsInAnyOrder(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testUserEquals() {
        TestUtil.equalsVerifier(UserEntity::class.java)
        val user1 = UserEntity()
        user1.id = 1L
        val user2 = UserEntity()
        user2.id = user1.id
        assertThat(user1).isEqualTo(user2)
        user2.id = 2L
        assertThat(user1).isNotEqualTo(user2)
        user1.id = null
        assertThat(user1).isNotEqualTo(user2)
    }

/*    @Test
    fun testUserFromId() {
        assertThat(userMapper!!.userFromId(DEFAULT_ID)!!.id).isEqualTo(DEFAULT_ID)
        assertThat(userMapper.userFromId(null)).isNull()
    }*/

/*    @Test
    fun testUserDTOtoUser() {
        val userDTO = UserDTO()
        userDTO.id = DEFAULT_ID
        userDTO.login = DEFAULT_LOGIN
        userDTO.firstName = DEFAULT_FIRSTNAME
        userDTO.lastName = DEFAULT_LASTNAME
        userDTO.email = DEFAULT_EMAIL
        userDTO.isActivated = true
        userDTO.imageUrl = DEFAULT_IMAGEURL
        userDTO.langKey = DEFAULT_LANGKEY
        userDTO.createdBy = DEFAULT_LOGIN
        userDTO.lastModifiedBy = DEFAULT_LOGIN
        userDTO.authorities = setOf(AuthoritiesConstants.USER)

        val user = userMapper!!.userDTOToUser(userDTO)
        assertThat(user!!.id).isEqualTo(DEFAULT_ID)
        assertThat(user.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(user.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(user.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(user.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(user.activated).isEqualTo(true)
        assertThat(user.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(user.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(user.createdBy).isNull()
        assertThat(user.createdDate).isNotNull()
        assertThat(user.lastModifiedBy).isNull()
        assertThat(user.lastModifiedDate).isNotNull()
        assertThat(user.authorities).extracting("name").containsExactly(AuthoritiesConstants.USER)
    }*/

/*    @Test
    fun testUserToUserDTO() {
        user!!.id = DEFAULT_ID
        user!!.createdBy = DEFAULT_LOGIN
        user!!.createdDate = Instant.now()
        user!!.lastModifiedBy = DEFAULT_LOGIN
        user!!.lastModifiedDate = Instant.now()
        val authorities = HashSet<Authority>()
        val authority = Authority()
        authority.name = AuthoritiesConstants.USER
        authorities.add(authority)
        user!!.authorities = authorities

        val userDTO = userMapper!!.userToUserDTO(user!!)

        assertThat(userDTO.id).isEqualTo(DEFAULT_ID)
        assertThat(userDTO.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(userDTO.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(userDTO.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(userDTO.isActivated).isEqualTo(true)
        assertThat(userDTO.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(userDTO.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(userDTO.createdBy).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.createdDate).isEqualTo(user!!.createdDate)
        assertThat(userDTO.lastModifiedBy).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.lastModifiedDate).isEqualTo(user!!.lastModifiedDate)
        assertThat(userDTO.authorities).containsExactly(AuthoritiesConstants.USER)
        assertThat(userDTO.toString()).isNotNull()
    }*/

    @Test
    @Throws(Exception::class)
    fun testAuthorityEquals() {
        val authorityA = AuthorityEntity()
        assertThat(authorityA).isEqualTo(authorityA)
        assertThat(authorityA).isNotEqualTo(null)
        assertThat(authorityA).isNotEqualTo(Any())
        assertThat(authorityA.hashCode()).isEqualTo(0)
        assertThat(authorityA.toString()).isNotNull()

        val authorityB = AuthorityEntity()
        assertThat(authorityA).isEqualTo(authorityB)

        authorityB.name = AuthoritiesConstants.ADMIN
        assertThat(authorityA).isNotEqualTo(authorityB)

        authorityA.name = AuthoritiesConstants.USER
        assertThat(authorityA).isNotEqualTo(authorityB)

        authorityB.name = AuthoritiesConstants.USER
        assertThat(authorityA).isEqualTo(authorityB)
        assertThat(authorityA.hashCode()).isEqualTo(authorityB.hashCode())
    }

    companion object {

        private const val DEFAULT_LOGIN = "johndoe"
        private val UPDATED_LOGIN = "jhipster"

        private val DEFAULT_ID = 1L

        private val DEFAULT_PASSWORD = "passjohndoe"
        private val UPDATED_PASSWORD = "passjhipster"

        private val DEFAULT_EMAIL = "johndoe@localhost"
        private val UPDATED_EMAIL = "jhipster@localhost"

        private val DEFAULT_FIRSTNAME = "john"
        private val UPDATED_FIRSTNAME = "jhipsterFirstName"

        private val DEFAULT_LASTNAME = "doe"
        private val UPDATED_LASTNAME = "jhipsterLastName"

        private val DEFAULT_IMAGEURL = "http://placehold.it/50x50"
        private val UPDATED_IMAGEURL = "http://placehold.it/40x40"

        private val DEFAULT_LANGKEY = "en"
        private val UPDATED_LANGKEY = "fr"

        /**
         * Create a User.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which has a required relationship to the User entity.
         */
        fun createEntity(em: EntityManager?): User {
            val user = User()
            user.login = DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5)
            user.password = RandomStringUtils.random(60)
            user.activated = true
            user.email = RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL
            user.firstName = DEFAULT_FIRSTNAME
            user.lastName = DEFAULT_LASTNAME
            user.imageUrl = DEFAULT_IMAGEURL
            user.langKey = DEFAULT_LANGKEY
            return user
        }
    }
}
