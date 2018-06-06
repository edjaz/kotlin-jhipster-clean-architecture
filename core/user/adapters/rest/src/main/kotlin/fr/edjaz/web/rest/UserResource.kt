package fr.edjaz.web.rest

import com.codahale.metrics.annotation.Timed
import fr.edjaz.web.config.Constants
import fr.edjaz.web.dto.UserDTO
import fr.edjaz.web.mapper.toFullUpdateRequest
import fr.edjaz.web.mapper.toMailUser
import fr.edjaz.web.mapper.toRequest
import fr.edjaz.web.service.rest.errors.BadRequestAlertException
import fr.edjaz.web.service.rest.errors.EmailAlreadyUsedException
import fr.edjaz.web.service.rest.errors.LoginAlreadyUsedException
import fr.edjaz.web.rest.util.HeaderUtil
import fr.edjaz.web.rest.util.PaginationUtil
import fr.edjaz.web.service.mail.SendCreationEmail
import fr.edjaz.web.service.mail.request.SendEmailUserRequest
import fr.edjaz.web.service.security.AuthoritiesConstants
import fr.edjaz.web.service.user.*
import io.github.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import javax.validation.Valid

/**
 * REST controller for managing users.
 *
 *
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 *
 *
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 *
 *
 * We use a View Model and a DTO for 3 reasons:
 *
 *  * We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.
 *  *  Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).
 *  *  As this manages users, for security reasons, we'd rather have a DTO layer.
 *
 *
 *
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
class UserResource(
                   private val getAuthorities: GetAuthorities
                   , private val createUser: CreateUser
                   , private val updateUser: UpdateUser
                   , private val deleteUser: DeleteUser
                   , private val sendCreationEmail: SendCreationEmail
                   , private val getAllManagedUsers: GetAllManagedUsers
                   , private val getUserWithAuthoritiesByLogin: GetUserWithAuthoritiesByLogin
                    , private val userSearch:UserSearch
) {

    private val log = LoggerFactory.getLogger(UserResource::class.java)

    /**
     * @return a string list of the all of the roles
     */
    @GetMapping("/users/authorities")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    fun getAuthorities(): List<String> = getAuthorities.execute()

    /**
     * POST  /users  : Creates a new user.
     *
     *
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException 400 (Bad Request) if the login or email is already in use
     */
    @PostMapping("/users")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    @Throws(URISyntaxException::class)
    fun createUser(@Valid @RequestBody userDTO: UserDTO): ResponseEntity<UserDTO> {
        log.debug("REST request to save User : {}", userDTO)
        val newUser = createUser.execute(userDTO.toRequest())
        sendCreationEmail.execute(SendEmailUserRequest(newUser.toMailUser()))
        return ResponseEntity.created(URI("/api/users/" + newUser.login!!))
            .headers(HeaderUtil.createAlert("userManagement.created", newUser.login!!))
            .body(UserDTO(newUser))

    }



    /**
     * PUT /users : Updates an existing User.
     *
     * @param userDTO the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already in use
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already in use
     */
    @PutMapping("/users")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    fun updateUser(@Valid @RequestBody userDTO: UserDTO): ResponseEntity<UserDTO> {
        log.debug("REST request to update User : {}", userDTO)

        val updatedUser = updateUser.execute(userDTO.toFullUpdateRequest())

        return ResponseUtil.wrapOrNotFound(updatedUser.map(::UserDTO),
            HeaderUtil.createAlert("userManagement.updated", userDTO.login!!))
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    fun getAllUsers(pageable: Pageable): ResponseEntity<List<UserDTO>> {
        val page = getAllManagedUsers.execute(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users")
        return ResponseEntity(page.content.map(::UserDTO), headers, HttpStatus.OK)
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    fun getUser(@PathVariable login: String): ResponseEntity<UserDTO> {
        log.debug("REST request to get User : {}", login)
        return ResponseUtil.wrapOrNotFound(
            getUserWithAuthoritiesByLogin.execute(login)
                .map({ UserDTO(it) }))
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    fun deleteUser(@PathVariable login: String): ResponseEntity<Void> {
        log.debug("REST request to delete User: {}", login)
        deleteUser.execute(login)
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login)).build()
    }

    /**
     * SEARCH /_search/users/:query : search for the User corresponding
     * to the query.
     *
     * @param query the query to search
     * @return the result of the search
     */
    @GetMapping("/_search/users/{query}")
    @Timed
    fun search(@PathVariable query: String): List<UserDTO> {
        return userSearch.execute(query).map { UserDTO(it) }
    }
}
