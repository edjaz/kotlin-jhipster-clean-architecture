package fr.edjaz.domain.model

import java.io.Serializable

/**
 * An authority (a fr.edjaz.rest.security role) used by Spring Security.
 */
data class Authority(
        var name: String? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val authority = other as Authority?

        return !if (name != null) name != authority!!.name else authority!!.name != null
    }

    override fun hashCode(): Int {
        return if (name != null) name!!.hashCode() else 0
    }

    override fun toString(): String {
        return "Authority{" +
                "name='" + name + '\''.toString() +
                "}"
    }
}
