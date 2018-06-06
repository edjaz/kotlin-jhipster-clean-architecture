package fr.edjaz.web.rest.vm

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * View Model object for storing a user's credentials.
 */
class LoginVM {

    @NotNull
    @Size(min = 1, max = 50)
    var username: String? = null

    @NotNull
    @Size(min = ManagedUserVM.PASSWORD_MIN_LENGTH, max = ManagedUserVM.PASSWORD_MAX_LENGTH)
    var password: String? = null

    var isRememberMe: Boolean? = null

    override fun toString(): String {
        return "LoginVM{" +
                "username='" + username + '\''.toString() +
                ", rememberMe=" + isRememberMe +
                '}'.toString()
    }
}
