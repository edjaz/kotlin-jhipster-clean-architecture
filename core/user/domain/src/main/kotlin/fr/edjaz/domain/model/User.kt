package fr.edjaz.domain.model


import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * A user.
 */
data class User(
        var id: Long? = null,
        var login: String? = null,
        var password: String? = null,
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var activated: Boolean = false,
        var langKey: String? = null,
        var imageUrl: String? = null,
        var activationKey: String? = null,
        var resetKey: String? = null,
        var resetDate: Instant? = null,
        var authorities: Set<Authority> = HashSet()
) : AbstractAuditing(), Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val user = other as User?
        return !(user!!.id == null || id == null) && id == user.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return "User{" +
                "login='" + login + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", lastName='" + lastName + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", imageUrl='" + imageUrl + '\''.toString() +
                ", activated='" + activated + '\''.toString() +
                ", langKey='" + langKey + '\''.toString() +
                ", activationKey='" + activationKey + '\''.toString() +
                "}"
    }
}
