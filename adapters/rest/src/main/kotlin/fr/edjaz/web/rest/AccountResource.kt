package fr.edjaz.web.rest

import com.codahale.metrics.annotation.Timed
import fr.edjaz.web.dto.UserDTO
import fr.edjaz.web.mapper.toMailUser
import fr.edjaz.web.mapper.toRequest
import fr.edjaz.web.mapper.toUpdateRequest
import fr.edjaz.web.rest.vm.KeyAndPasswordVM
import fr.edjaz.web.rest.vm.ManagedUserVM
import fr.edjaz.web.service.mail.SendActivationEmail
import fr.edjaz.web.service.mail.SendPasswordResetMail
import fr.edjaz.web.service.mail.request.SendEmailUserRequest
import fr.edjaz.web.service.rest.errors.EmailNotFoundException
import fr.edjaz.web.service.rest.errors.InternalServerErrorException
import fr.edjaz.web.service.rest.errors.InvalidPasswordException
import fr.edjaz.web.service.user.*
import fr.edjaz.web.service.user.request.CompletePasswordResetRequest
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
class AccountResource(
                      private val getUserWithAuthorities: GetUserWithAuthorities
                      , private val activateRegistration: ActivateRegistration
                      , private val completePasswordReset: CompletePasswordReset
                      , private val requestPasswordReset: RequestPasswordReset
                      , private val registerUser: RegisterUser
                      , private val changePassword: ChangePassword
                      , private val updateUser: UpdateUser
                      , private val sendActivationEmail: SendActivationEmail
                      , private val sendPasswordResetMail: SendPasswordResetMail
) {

    private val log = LoggerFactory.getLogger(AccountResource::class.java)

    /**
     * GET  /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    fun getAccount(): UserDTO = getUserWithAuthorities.execute()
        .map { UserDTO(it) }
        .orElseThrow { InternalServerErrorException("User could not be found") }

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
     */
    @PostMapping("/register")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    fun registerAccount(@Valid @RequestBody managedUserVM: ManagedUserVM) {
        if (!checkPasswordLength(managedUserVM.password)) {
            throw InvalidPasswordException()
        }
        val user = registerUser.execute(managedUserVM.toRequest())
        sendActivationEmail.execute(SendEmailUserRequest(user.toMailUser()))
    }


    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    fun activateAccount(@RequestParam(value = "key") key: String) {
        val user = activateRegistration.execute(key)
        if (!user.isPresent) {
            throw InternalServerErrorException("No user was found for this reset key")
        }
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    fun isAuthenticated(request: HttpServletRequest): String? {
        log.debug("REST request to check if the current user is authenticated")
        return request.remoteUser
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PostMapping("/account")
    @Timed
    fun saveAccount(@Valid @RequestBody userDTO: UserDTO) {
        updateUser.execute(userDTO.toUpdateRequest())
    }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param password the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = arrayOf("/account/change-password"))
    @Timed
    fun changePassword(@RequestBody password: String) {
        if (!checkPasswordLength(password)) {
            throw InvalidPasswordException()
        }
        changePassword.execute(password)
    }

    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = arrayOf("/account/reset-password/init"))
    @Timed
    fun requestPasswordReset(@RequestBody mail: String) {
        sendPasswordResetMail.execute(
            SendEmailUserRequest(
                requestPasswordReset.execute(mail)
                    .orElseThrow<EmailNotFoundException>({ EmailNotFoundException() }).toMailUser()
            )
        )
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = arrayOf("/account/reset-password/finish"))
    @Timed
    fun finishPasswordReset(@RequestBody keyAndPassword: KeyAndPasswordVM) {
        if (!checkPasswordLength(keyAndPassword.newPassword)) {
            throw InvalidPasswordException()
        }
        val user = completePasswordReset.execute(CompletePasswordResetRequest(keyAndPassword.newPassword!!, keyAndPassword.key!!))

        if (!user.isPresent) {
            throw InternalServerErrorException("No user was found for this reset key")
        }
    }

    private fun checkPasswordLength(password: String?): Boolean {
        return !StringUtils.isEmpty(password) &&
            password!!.length >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length <= ManagedUserVM.PASSWORD_MAX_LENGTH
    }
}
