package fr.edjaz.web.dto

import fr.edjaz.web.config.Constants
import fr.edjaz.web.service.user.response.UserModelResponse
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import java.time.Instant
import java.util.stream.Collectors
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A DTO representing a user, with his authorities.
 */
open class UserDTO {

    var id: Long? = null

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 100)
    var login: String? = null

    @Size(max = 50)
    var firstName: String? = null

    @Size(max = 50)
    var lastName: String? = null

    @Email
    @Size(min = 5, max = 100)
    var email: String? = null

    @Size(max = 256)
    var imageUrl: String? = null

    var isActivated = false

    @Size(min = 2, max = 6)
    var langKey: String? = null

    var createdBy: String? = null

    var createdDate: Instant? = null

    var lastModifiedBy: String? = null

    var lastModifiedDate: Instant? = null

    var authorities: Set<String>? = null

    constructor() {
        // Empty constructor needed for Jackson.
    }

    constructor(user: UserModelResponse) {
        this.id = user.id
        this.login = user.login
        this.firstName = user.firstName
        this.lastName = user.lastName
        this.email = user.email
        this.isActivated = user.activated!!
        this.imageUrl = user.imageUrl
        this.langKey = user.langKey
        this.createdBy = user.createdBy
        this.createdDate = user.createdDate
        this.lastModifiedBy = user.lastModifiedBy
        this.lastModifiedDate = user.lastModifiedDate
        this.authorities = user.authorities!!.stream()
            .map<String>({ it.name })
            .collect(Collectors.toSet())
    }

/*    constructor(user: User) {
        this.id = user.id
        this.login = user.login
        this.firstName = user.firstName
        this.lastName = user.lastName
        this.email = user.email
        this.isActivated = user.activated
        this.imageUrl = user.imageUrl
        this.langKey = user.langKey
        this.createdBy = user.createdBy
        this.createdDate = user.createdDate
        this.lastModifiedBy = user.lastModifiedBy
        this.lastModifiedDate = user.lastModifiedDate
        this.authorities = user.authorities.stream()
            .map<String>({ it.name })
            .collect(Collectors.toSet())
    }*/

    override fun toString(): String {
        return "UserDTO{" +
            "login='" + login + '\''.toString() +
            ", firstName='" + firstName + '\''.toString() +
            ", lastName='" + lastName + '\''.toString() +
            ", email='" + email + '\''.toString() +
            ", imageUrl='" + imageUrl + '\''.toString() +
            ", activated=" + isActivated +
            ", langKey='" + langKey + '\''.toString() +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", lastModifiedBy='" + lastModifiedBy + '\''.toString() +
            ", lastModifiedDate=" + lastModifiedDate +
            ", authorities=" + authorities +
            "}"
    }
}
