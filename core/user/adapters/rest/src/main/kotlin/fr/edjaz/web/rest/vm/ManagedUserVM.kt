package fr.edjaz.web.rest.vm

import fr.edjaz.web.dto.UserDTO
import javax.validation.constraints.Size

/**
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 */
class ManagedUserVM : UserDTO() {

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    var password: String? = null

    override fun toString(): String {
        return "ManagedUserVM{" +
                "} " + super.toString()
    }

    companion object {

        const val PASSWORD_MIN_LENGTH = 4

        const val PASSWORD_MAX_LENGTH = 100
    }
}// Empty constructor needed for Jackson.
